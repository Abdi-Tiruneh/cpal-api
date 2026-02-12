package com.commercepal.apiservice.users.auth;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.exceptions.security.UnauthorizedException;
import com.commercepal.apiservice.shared.security.CustomUserDetailsService;
import com.commercepal.apiservice.shared.security.JwtTokenProvider;
import com.commercepal.apiservice.users.auth.dto.AuthResponse;
import com.commercepal.apiservice.users.auth.dto.LoginRequest;
import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.credential.CredentialRepository;
import com.commercepal.apiservice.users.enums.UserType;
import com.commercepal.apiservice.utils.ClientIpUtils;
import com.commercepal.apiservice.utils.DeviceFingerprintUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enhanced authentication service with refresh token support and comprehensive
 * security.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService userDetailsService;
  private final CredentialRepository credentialRepository;

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
    log.info("[AUTH] Login attempt initiated for user {} from channel {}",
        request.loginIdentifier(), request.channel());

    try {
      // 1. Check for Staff restriction first
      Credential credential = credentialRepository.findByIdentifier(request.loginIdentifier())
          .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

      if (credential.getUserType() == UserType.STAFF) {
        log.warn("[AUTH] Staff user attempted login via public endpoint | loginIdentifier={}",
            request.loginIdentifier());
        throw new UnauthorizedException("Invalid credentials");
      }

      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.loginIdentifier(), request.password()));

      String deviceFingerprint = DeviceFingerprintUtils.extractDeviceFingerprint(httpRequest);
      String ipAddress = ClientIpUtils.getClientIpAddress(httpRequest);

      JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.generateTokenPair(authentication,
          deviceFingerprint, ipAddress, request.loginIdentifier());

      userDetailsService.updateLastActivity(request.loginIdentifier());

      // Reset failed login attempts on successful login
      userDetailsService.resetFailedLoginAttempts(request.loginIdentifier());

      log.info("[AUTH] User {} logged in successfully from channel {}",
          request.loginIdentifier(), request.channel());

      return new AuthResponse(tokenPair.getAccessToken(), tokenPair.getRefreshToken(),
          tokenPair.getTokenType(), tokenPair.getExpiresIn());

    } catch (AuthenticationException e) {
      String ipAddress = ClientIpUtils.getClientIpAddress(httpRequest);
      String deviceFingerprint = DeviceFingerprintUtils.extractDeviceFingerprint(httpRequest);

      log.error("[AUTH] Authentication failed | loginIdentifier={} | channel={} | error={}",
          request.loginIdentifier(), request.channel(), e.getMessage());

      // Use enhanced protection service with IP and device tracking
      userDetailsService.incrementFailedLoginAttempts(request.loginIdentifier(), ipAddress,
          deviceFingerprint);

      throw new UnauthorizedException("Invalid credentials");
    }
  }

  /**
   * Refresh access token using refresh token
   */
  @Transactional(readOnly = true)
  public AuthResponse refreshToken(String refreshToken, Channel channel,
      HttpServletRequest httpRequest) {
    log.info("[AUTH] Token refresh attempt initiated from channel {}", channel);

    try {
      // Restriction: Check if user is STAFF
      String loginIdentifier = jwtTokenProvider.getUsernameFromToken(refreshToken);
      if (loginIdentifier != null) {
        credentialRepository.findByIdentifier(loginIdentifier).ifPresent(credential -> {
          if (credential.getUserType() == UserType.STAFF) {
            log.warn("[AUTH] Staff user attempted refresh via public endpoint | loginIdentifier={}", loginIdentifier);
            throw new UnauthorizedException("Invalid credentials");
          }
        });
      }

      String deviceFingerprint = DeviceFingerprintUtils.extractDeviceFingerprint(httpRequest);
      String ipAddress = ClientIpUtils.getClientIpAddress(httpRequest);

      JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.refreshAccessToken(refreshToken,
          deviceFingerprint, ipAddress);

      log.info("[AUTH] Token refreshed successfully from channel {}", channel);

      return new AuthResponse(tokenPair.getAccessToken(), tokenPair.getRefreshToken(),
          tokenPair.getTokenType(), tokenPair.getExpiresIn());

    } catch (Exception e) {
      log.error("[AUTH] Token refresh failed | channel={} | error={}", channel, e.getMessage());
      throw new UnauthorizedException("Invalid or expired refresh token");
    }
  }

  /**
   * Logout - blacklist tokens
   */
  public void logout(String accessToken) {
    log.info("[AUTH] Logout attempt initiated");
    jwtTokenProvider.blacklistToken(accessToken, "USER_LOGOUT");
    log.info("[AUTH] User logged out successfully");
  }
}
