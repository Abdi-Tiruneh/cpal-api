package com.commercepal.apiservice.users.auth;

import com.commercepal.apiservice.shared.exceptions.security.UnauthorizedException;
import com.commercepal.apiservice.shared.security.CustomUserDetailsService;
import com.commercepal.apiservice.shared.security.JwtTokenProvider;
import com.commercepal.apiservice.shared.security.check.RateLimitingService;
import com.commercepal.apiservice.users.auth.dto.AuthResponse;
import com.commercepal.apiservice.users.auth.dto.LoginRequest;
import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.credential.CredentialRepository;
import com.commercepal.apiservice.users.enums.UserType;
import com.commercepal.apiservice.utils.ClientIpUtils;
import com.commercepal.apiservice.utils.DeviceFingerprintUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Dedicated authentication service for staff members (Admins, Operations,
 * etc.).
 * Strictly enforce zero-trust principles and isolated logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StaffAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RateLimitingService rateLimitingService;
    private final CredentialRepository credentialRepository;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = ClientIpUtils.getClientIpAddress(httpRequest);
        String deviceFingerprint = DeviceFingerprintUtils.extractDeviceFingerprint(httpRequest);

        // 1. Audit Log - Start
        log.info("[STAFF_AUTH] Login attempt initiated | loginIdentifier={} | ip={}",
                request.loginIdentifier(), ipAddress);

        // 2. Rate Limiting - Aggressive
        String rateLimitKey = "staff:auth:" + ipAddress;
        if (!rateLimitingService.isRequestAllowed(rateLimitKey, RateLimitingService.RateLimitType.LOGIN_ATTEMPT)) {
            log.warn("[STAFF_AUTH] Rate limit exceeded for IP: {}", ipAddress);
            rateLimitingService.blockIdentifier(ipAddress, Duration.ofMinutes(15), "Excessive staff login attempts");
            throw new UnauthorizedException("Too many login attempts. Please try again later.");
        }

        if (rateLimitingService.isBlocked(ipAddress)) {
            log.warn("[STAFF_AUTH] Blocked IP attempted login: {}", ipAddress);
            throw new UnauthorizedException("Access temporarily blocked due to suspicious activity.");
        }

        try {
            Credential credential = credentialRepository.findByIdentifier(request.loginIdentifier())
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

            if (credential.getUserType() != UserType.STAFF) {
                log.warn("[STAFF_AUTH] Non-staff user attempted staff login | loginIdentifier={} | userType={}",
                        request.loginIdentifier(), credential.getUserType());
                // Treat as failed login
                throw new UnauthorizedException("Invalid credentials");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.loginIdentifier(), request.password()));

            // 4. Generate Tokens
            JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.generateTokenPair(authentication,
                    deviceFingerprint, ipAddress, request.loginIdentifier());

            userDetailsService.updateLastActivity(request.loginIdentifier());
            userDetailsService.resetFailedLoginAttempts(request.loginIdentifier());

            log.info("[STAFF_AUTH] Staff user logged in successfully | loginIdentifier={} | role=STAFF",
                    request.loginIdentifier());

            return new AuthResponse(tokenPair.getAccessToken(), tokenPair.getRefreshToken(),
                    tokenPair.getTokenType(), tokenPair.getExpiresIn());

        } catch (AuthenticationException e) {
            log.error("[STAFF_AUTH] Authentication failed | loginIdentifier={} | ip={} | error={}",
                    request.loginIdentifier(), ipAddress, e.getMessage());

            userDetailsService.incrementFailedLoginAttempts(request.loginIdentifier(), ipAddress, deviceFingerprint);

            throw new UnauthorizedException("Invalid credentials");
        }
    }

    public void logout(String accessToken) {
        log.info("[STAFF_AUTH] Logout attempt initiated");
        jwtTokenProvider.blacklistToken(accessToken, "STAFF_LOGOUT");
        log.info("[STAFF_AUTH] Staff user logged out successfully");
    }

    public AuthResponse refreshToken(String refreshToken, HttpServletRequest httpRequest) {
        String ipAddress = ClientIpUtils.getClientIpAddress(httpRequest);
        String deviceFingerprint = DeviceFingerprintUtils.extractDeviceFingerprint(httpRequest);

        log.info("[STAFF_AUTH] Token refresh attempt initiated | ip={}", ipAddress);

        try {
            // 1. Extract username/subject from token (implicitly validates signature)
            String loginIdentifier = jwtTokenProvider.getUsernameFromToken(refreshToken);

            if (loginIdentifier == null) {
                log.warn("[STAFF_AUTH] Failed to extract subject from refresh token");
                throw new UnauthorizedException("Invalid refresh token");
            }

            // 2. Strict Role Check
            Credential credential = credentialRepository.findByIdentifier(loginIdentifier)
                    .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

            if (credential.getUserType() != UserType.STAFF) {
                log.warn("[STAFF_AUTH] Non-staff user attempted token refresh | loginIdentifier={} | userType={}",
                        loginIdentifier, credential.getUserType());
                throw new UnauthorizedException("Invalid refresh token");
            }

            // 3. Delegate to provider for full refresh logic (checks expiry, blacklist,
            // sessions, etc.)
            JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.refreshAccessToken(refreshToken, deviceFingerprint,
                    ipAddress);

            log.info("[STAFF_AUTH] Token refreshed successfully | loginIdentifier={}", loginIdentifier);

            return new AuthResponse(tokenPair.getAccessToken(), tokenPair.getRefreshToken(),
                    tokenPair.getTokenType(), tokenPair.getExpiresIn());

        } catch (JwtException | UnauthorizedException e) {
            log.error("[STAFF_AUTH] Token refresh failed | ip={} | error={}", ipAddress, e.getMessage());
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }
}
