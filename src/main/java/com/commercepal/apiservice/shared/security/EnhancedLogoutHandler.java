package com.commercepal.apiservice.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * Enhanced logout handler
 */
public record EnhancedLogoutHandler(JwtTokenProvider jwtTokenProvider,
                                    SecurityAuditService securityAuditService) implements
    LogoutHandler {

  @Override
  public void logout(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) {

    String token = extractTokenFromRequest(request);
    if (token != null) {
      // Blacklist the token
      jwtTokenProvider.blacklistToken(token, SecurityConfigConstants.LOGOUT_AUDIT_REASON);

      // Audit logout
      if (authentication != null) {
        securityAuditService.auditTokenBlacklist(authentication.getName(),
            SecurityConfigConstants.LOGOUT_AUDIT_TOKEN_TYPE,
            SecurityConfigConstants.LOGOUT_AUDIT_REASON);
      }
    }
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader(SecurityConfigConstants.HEADER_AUTHORIZATION);
    if (bearerToken != null && bearerToken.startsWith(SecurityConfigConstants.BEARER_PREFIX)) {
      return bearerToken.substring(SecurityConfigConstants.BEARER_PREFIX.length());
    }
    return null;
  }
}
