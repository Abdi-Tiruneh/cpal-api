package com.commercepal.apiservice.users.auth;

import com.commercepal.apiservice.users.auth.dto.AuthResponse;
import com.commercepal.apiservice.users.auth.dto.LoginRequest;
import com.commercepal.apiservice.users.auth.dto.RefreshTokenRequest;
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
 * Dedicated REST controller for staff authentication.
 * Completely isolated from customer authentication flows.
 */
@RestController
@RequestMapping("/api/v1/staff/auth")
@RequiredArgsConstructor
@Tag(name = "Staff Authentication", description = "Secured endpoints for administrative and operations staff login.")
public class StaffAuthController {

    private final StaffAuthService staffAuthService;

    @Operation(summary = "Staff Login", description = "Authenticate staff users. Enforces strict role checks and aggressive rate limiting.", responses = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or unauthorized role"),
            @ApiResponse(responseCode = "429", description = "Too many login attempts")
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

        AuthResponse authResponse = staffAuthService.login(request, httpRequest);

        return ResponseWrapper.success("Staff login successful", authResponse);
    }

    @Operation(summary = "Staff Refresh Token", description = "Refresh access token using refresh token. Enforces staff role check.", responses = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ResponseWrapper<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {

        AuthResponse authResponse = staffAuthService.refreshToken(request.refreshToken(), httpRequest);

        return ResponseWrapper.success("Token refreshed successfully", authResponse);
    }

    @Operation(summary = "Staff Logout", description = "Logout staff user and invalidate tokens.")
    @PostMapping("/logout")
    public ResponseEntity<ResponseWrapper<Void>> logout(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            staffAuthService.logout(token);
        }
        return ResponseWrapper.success("Logout successful");
    }
}
