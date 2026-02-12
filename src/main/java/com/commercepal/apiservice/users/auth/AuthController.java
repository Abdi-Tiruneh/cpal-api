package com.commercepal.apiservice.users.auth;

import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.auth.dto.AuthResponse;
import com.commercepal.apiservice.users.auth.dto.LoginRequest;
import com.commercepal.apiservice.users.auth.dto.OAuth2CompleteProfileRequest;
import com.commercepal.apiservice.users.auth.dto.OAuth2LoginRequest;
import com.commercepal.apiservice.users.auth.dto.OAuth2LoginResponse;
import com.commercepal.apiservice.users.auth.dto.OAuth2SetPasswordRequest;
import com.commercepal.apiservice.users.auth.dto.RefreshTokenRequest;
import com.commercepal.apiservice.utils.CurrentUserService;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations with enhanced security.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Operations related to user authentication, including web and mobile logins with refresh token support.")
public class AuthController {

  private final AuthenticationService authenticationService;
  private final OAuth2Service oAuth2Service;
  private final CurrentUserService currentUserService;

  /**
   * User login for all user types via email or phone and password.
   *
   * @param request     Login request payload
   * @param httpRequest HTTP request for IP and device tracking
   * @return AuthResponse wrapped in a standard response format
   */
  @Operation(summary = "User login", description = "Authenticate customers, merchants, and agents. Returns access token and refresh token.", responses = {
      @ApiResponse(responseCode = "200", description = "Successful authentication", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation error or bad request"),
      @ApiResponse(responseCode = "401", description = "Invalid credentials") })
  @PostMapping("/login")
  public ResponseEntity<ResponseWrapper<AuthResponse>> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

    AuthResponse response = authenticationService.login(request, httpRequest);
    return ResponseWrapper.success("Login successful", response);
  }

  /**
   * Refresh access token using refresh token.
   *
   * @param request     Refresh token request payload
   * @param httpRequest HTTP request for IP and device tracking
   * @return AuthResponse with new access token and refresh token
   */
  @Operation(summary = "Refresh token", description = "Refresh access token using refresh token. Returns new access token and refresh token pair.", responses = {
      @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation error or bad request"),
      @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token") })
  @PostMapping("/refresh")
  public ResponseEntity<ResponseWrapper<AuthResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {

    AuthResponse response = authenticationService.refreshToken(request.refreshToken(),
        request.channel(), httpRequest);
    return ResponseWrapper.success("Token refreshed successfully", response);
  }

  /**
   * Logout - invalidate tokens.
   *
   * @param httpRequest HTTP request to extract token
   * @return Success response
   */
  @Operation(summary = "Logout", description = "Logout user and invalidate access token. Refresh token is also invalidated.", responses = {
      @ApiResponse(responseCode = "200", description = "Logout successful"),
      @ApiResponse(responseCode = "401", description = "Unauthorized") })
  @PostMapping("/logout")
  public ResponseEntity<ResponseWrapper<Object>> logout(HttpServletRequest httpRequest) {
    String authHeader = httpRequest.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      authenticationService.logout(token);
    }
    return ResponseWrapper.success("Logout successful");
  }

  // OAuth2 Endpoints

  @Operation(summary = "OAuth2 Login", description = "Login or register user using OAuth2 provider (Google, Apple, Facebook)")
  @PostMapping("/oauth2/login")
  public ResponseEntity<ResponseWrapper<OAuth2LoginResponse>> oauth2Login(
      @RequestBody @Valid OAuth2LoginRequest request,
      HttpServletRequest httpRequest) {

    OAuth2LoginResponse response = oAuth2Service.processOAuth2Login(request, httpRequest);
    return ResponseWrapper.success("OAuth2 login successful", response);
  }

  @Operation(summary = "Complete User Profile", description = "Provide missing phone number or email for OAuth2 accounts (Required for Ethiopian customers)")
  @PostMapping("/oauth2/complete-profile")
  public ResponseEntity<ResponseWrapper<OAuth2LoginResponse>> completeProfile(
      @RequestBody @Valid OAuth2CompleteProfileRequest request) {

    Credential credential = currentUserService.getCurrentUser();

    OAuth2LoginResponse response = oAuth2Service.completeProfile(request, credential);
    return ResponseWrapper.success("Profile updated successfully", response);
  }

  @Operation(summary = "Set Optional Password", description = "Allow OAuth2 users to set a password for traditional login")
  @PostMapping("/oauth2/set-password")
  public ResponseEntity<ResponseWrapper<Void>> setPassword(
      @RequestBody @Valid OAuth2SetPasswordRequest request) {

    Credential credential = currentUserService.getCurrentUser();

    oAuth2Service.setPassword(request, credential);
    return ResponseWrapper.success("Password set successfully");
  }
}
