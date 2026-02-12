package com.commercepal.apiservice.shared.security.check;//package com.fastpay.agent.shared.security;
//
//import com.fastpay.agent.core_feature.auth.dto.LoginRequest;
//import com.fastpay.agent.core_feature.auth.AuthResponse;
//import com.fastpay.agent.user.device_tracking.*;
//import com.fastpay.agent.user.device_tracking.dto.*;
//import com.fastpay.agent.user.user_profile.User;
//import com.fastpay.agent.util.CurrentUserService;
//import com.fastpay.agent.util.ClientIpUtils;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.validation.Valid;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
/// **
// * Enhanced authentication controller with device tracking integration.
// * Provides comprehensive authentication with device and security monitoring.
// */
//@RestController
//@RequestMapping("/api/v1/auth")
//@RequiredArgsConstructor
//@Slf4j
//public class EnhancedAuthenticationController {
//
//    private final AuthenticationManager authenticationManager;
//    private final JwtTokenProvider jwtTokenProvider;
//    private final DeviceTrackingService deviceTrackingService;
//    private final CurrentUserService currentUserService;
//    private final ClientIpUtils clientIpUtils;
//    private final SecurityAuditService securityAuditService;
//
//    /**
//     * Enhanced login with device tracking
//     */
//    @PostMapping("/login")
//    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
//                                                     HttpServletRequest httpRequest) {
//        log.info("Enhanced login attempt for user: {}", request.getUsername());
//
//        try {
//            // Authenticate user
//            Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
//            );
//
//            // Generate JWT token
//            String token = jwtTokenProvider.generateToken(authentication);
//
//            // Get current user
//            User currentUser = currentUserService.getCurrentUser();
//
//            // Extract device information
//            DeviceInfo deviceInfo = extractDeviceInfo(httpRequest, request);
//
//            // Process device tracking
//            DeviceTrackingResult trackingResult = processDeviceTracking(currentUser, deviceInfo, httpRequest);
//
//            // Create response
//            Map<String, Object> response = new HashMap<>();
//            response.put("access_token", token);
//            response.put("token_type", "Bearer");
//            response.put("expires_in", jwtTokenProvider.getTokenValidityInSeconds());
//            response.put("user", createUserResponse(currentUser));
//            response.put("device", createDeviceResponse(trackingResult.getDevice()));
//            response.put("session", createSessionResponse(trackingResult.getSession()));
//            response.put("security", createSecurityResponse(trackingResult));
//
//            // Audit successful login
//            securityAuditService.auditLoginSuccess(currentUser.getId(),
//                clientIpUtils.getClientIpAddress(httpRequest),
//                deviceInfo.getDeviceFingerprint());
//
//            log.info("Enhanced login successful for user: {} on device: {}",
//                    request.getUsername(), deviceInfo.getDeviceFingerprint());
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("Enhanced login failed for user: {} - {}", request.getUsername(), e.getMessage());
//
//            // Audit failed login
//            securityAuditService.auditLoginFailure(request.getUsername(),
//                clientIpUtils.getClientIpAddress(httpRequest),
//                e.getMessage());
//
//            throw e;
//        }
//    }
//
//    /**
//     * Enhanced registration with device tracking
//     */
//    @PostMapping("/register")
//    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody EnhancedRegistrationRequest request,
//                                                       HttpServletRequest httpRequest) {
//        log.info("Enhanced registration attempt for user: {}", request.getEmail());
//
//        try {
//            // Process user registration (implement your registration logic here)
//            User newUser = processUserRegistration(request);
//
//            // Extract device information
//            DeviceInfo deviceInfo = extractDeviceInfo(httpRequest, request);
//
//            // Process device tracking for new user
//            DeviceTrackingResult trackingResult = processDeviceTracking(newUser, deviceInfo, httpRequest);
//
//            // Create response
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Registration successful");
//            response.put("user", createUserResponse(newUser));
//            response.put("device", createDeviceResponse(trackingResult.getDevice()));
//            response.put("security", createSecurityResponse(trackingResult));
//
//            // Audit successful registration
//            securityAuditService.auditRegistrationSuccess(newUser.getId(),
//                clientIpUtils.getClientIpAddress(httpRequest),
//                deviceInfo.getDeviceFingerprint());
//
//            log.info("Enhanced registration successful for user: {} on device: {}",
//                    request.getEmail(), deviceInfo.getDeviceFingerprint());
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("Enhanced registration failed for user: {} - {}", request.getEmail(), e.getMessage());
//
//            // Audit failed registration
//            securityAuditService.auditRegistrationFailure(request.getEmail(),
//                clientIpUtils.getClientIpAddress(httpRequest),
//                e.getMessage());
//
//            throw e;
//        }
//    }
//
//    /**
//     * Logout with device session cleanup
//     */
//    @PostMapping("/logout")
//    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest httpRequest) {
//        log.info("Enhanced logout request");
//
//        try {
//            User currentUser = currentUserService.getCurrentUser();
//            String sessionId = extractSessionId(httpRequest);
//
//            // End device session
//            if (sessionId != null) {
//                deviceTrackingService.endDeviceSession(sessionId, "User logout");
//            }
//
//            // Clear security context
//            SecurityContextHolder.clearContext();
//
//            // Audit logout
//            securityAuditService.auditLogout(currentUser.getId(),
//                clientIpUtils.getClientIpAddress(httpRequest));
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Logout successful");
//
//            log.info("Enhanced logout successful for user: {}", currentUser.getId());
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("Enhanced logout failed: {}", e.getMessage());
//            throw e;
//        }
//    }
//
//    /**
//     * Get current user with device information
//     */
//    @GetMapping("/me")
//    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest httpRequest) {
//        log.info("Getting current user with device information");
//
//        try {
//            User currentUser = currentUserService.getCurrentUser();
//
//            // Get device analytics
//            DeviceAnalytics analytics = deviceTrackingService.getUserDeviceAnalytics(currentUser);
//
//            // Get active sessions
//            List<DeviceSession> activeSessions = deviceTrackingService.getActiveSessions(currentUser);
//
//            // Get recent security events
//            List<DeviceSecurityEvent> recentEvents = deviceTrackingService.getHighRiskSecurityEvents(currentUser);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("user", createUserResponse(currentUser));
//            response.put("analytics", analytics);
//            response.put("activeSessions", activeSessions.size());
//            response.put("recentSecurityEvents", recentEvents.size());
//            response.put("securityScore", calculateUserSecurityScore(currentUser, analytics));
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            log.error("Failed to get current user: {}", e.getMessage());
//            throw e;
//        }
//    }
//
//    // HELPER METHODS
//
//    /**
//     * Extract device information from request
//     */
//    private DeviceInfo extractDeviceInfo(HttpServletRequest httpRequest, Object request) {
//        String userAgent = httpRequest.getHeader("User-Agent");
//        String ipAddress = clientIpUtils.getClientIpAddress(httpRequest);
//        String deviceFingerprint = generateDeviceFingerprint(httpRequest);
//
//        return DeviceInfo.builder()
//                .deviceFingerprint(deviceFingerprint)
//                .userAgent(userAgent)
//                .ipAddress(ipAddress)
//                .deviceType(determineDeviceType(userAgent))
//                .operatingSystem(extractOperatingSystem(userAgent))
//                .browser(extractBrowser(userAgent))
//                .isVpn(detectVpn(httpRequest))
//                .isProxy(detectProxy(httpRequest))
//                .isTor(detectTor(httpRequest))
//                .isDataCenter(detectDataCenter(ipAddress))
//                .isMobile(detectMobile(userAgent))
//                .build();
//    }
//
//    /**
//     * Process device tracking for user
//     */
//    private DeviceTrackingResult processDeviceTracking(User user, DeviceInfo deviceInfo, HttpServletRequest httpRequest) {
//        // Check if device exists
//        Optional<Device> existingDevice = deviceTrackingService.getDeviceByFingerprint(deviceInfo.getDeviceFingerprint());
//
//        Device device;
//        if (existingDevice.isPresent()) {
//            device = existingDevice.get();
//        } else {
//            // Register new device
//            DeviceRegistrationRequest deviceRequest = createDeviceRegistrationRequest(deviceInfo, httpRequest);
//            device = deviceTrackingService.registerDevice(user, deviceRequest);
//        }
//
//        // Start device session
//        SessionStartRequest sessionRequest = createSessionStartRequest(deviceInfo, httpRequest);
//        DeviceSession session = deviceTrackingService.startDeviceSession(user, device, sessionRequest);
//
//        return DeviceTrackingResult.builder()
//                .device(device)
//                .session(session)
//                .isNewDevice(!existingDevice.isPresent())
//                .isHighRisk(device.isHighRisk())
//                .requiresVerification(device.needsVerification())
//                .build();
//    }
//
//    /**
//     * Create device registration request
//     */
//    private DeviceRegistrationRequest createDeviceRegistrationRequest(DeviceInfo deviceInfo, HttpServletRequest httpRequest) {
//        return DeviceRegistrationRequest.builder()
//                .deviceFingerprint(deviceInfo.getDeviceFingerprint())
//                .deviceType(deviceInfo.getDeviceType())
//                .operatingSystem(deviceInfo.getOperatingSystem())
//                .browser(deviceInfo.getBrowser())
//                .ipAddress(deviceInfo.getIpAddress())
//                .userAgent(deviceInfo.getUserAgent())
//                .isVpn(deviceInfo.getIsVpn())
//                .isProxy(deviceInfo.getIsProxy())
//                .isTor(deviceInfo.getIsTor())
//                .isDataCenter(deviceInfo.getIsDataCenter())
//                .isMobile(deviceInfo.getIsMobile())
//                .build();
//    }
//
//    /**
//     * Create session start request
//     */
//    private SessionStartRequest createSessionStartRequest(DeviceInfo deviceInfo, HttpServletRequest httpRequest) {
//        return SessionStartRequest.builder()
//                .sessionType(SessionType.LOGIN)
//                .ipAddress(deviceInfo.getIpAddress())
//                .userAgent(deviceInfo.getUserAgent())
//                .isSecureConnection(httpRequest.isSecure())
//                .isVpn(deviceInfo.getIsVpn())
//                .isProxy(deviceInfo.getIsProxy())
//                .isTor(deviceInfo.getIsTor())
//                .build();
//    }
//
//    /**
//     * Create user response
//     */
//    private Map<String, Object> createUserResponse(User user) {
//        Map<String, Object> userResponse = new HashMap<>();
//        userResponse.put("id", user.getId());
//        userResponse.put("username", user.getUsername());
//        userResponse.put("email", user.getEmail());
//        userResponse.put("firstName", user.getFirstName());
//        userResponse.put("lastName", user.getLastName());
//        userResponse.put("userType", user.getUserType());
//        userResponse.put("userStatus", user.getUserStatus());
//        userResponse.put("lastLoginAt", user.getLastLoginAt());
//        return userResponse;
//    }
//
//    /**
//     * Create device response
//     */
//    private Map<String, Object> createDeviceResponse(Device device) {
//        Map<String, Object> deviceResponse = new HashMap<>();
//        deviceResponse.put("id", device.getId());
//        deviceResponse.put("deviceFingerprint", device.getDeviceFingerprint());
//        deviceResponse.put("deviceType", device.getDeviceType());
//        deviceResponse.put("operatingSystem", device.getOperatingSystem());
//        deviceResponse.put("browser", device.getBrowser());
//        deviceResponse.put("trustScore", device.getTrustScore());
//        deviceResponse.put("riskScore", device.getRiskScore());
//        deviceResponse.put("isTrusted", device.getIsTrusted());
//        deviceResponse.put("isVerified", device.getIsVerified());
//        deviceResponse.put("isBlocked", device.getIsBlocked());
//        deviceResponse.put("lastSeenAt", device.getLastSeenAt());
//        return deviceResponse;
//    }
//
//    /**
//     * Create session response
//     */
//    private Map<String, Object> createSessionResponse(DeviceSession session) {
//        Map<String, Object> sessionResponse = new HashMap<>();
//        sessionResponse.put("sessionId", session.getSessionId());
//        sessionResponse.put("sessionType", session.getSessionType());
//        sessionResponse.put("sessionStatus", session.getSessionStatus());
//        sessionResponse.put("sessionStart", session.getSessionStart());
//        sessionResponse.put("securityScore", session.getSecurityScore());
//        sessionResponse.put("riskScore", session.getRiskScore());
//        sessionResponse.put("isSecureConnection", session.getIsSecureConnection());
//        return sessionResponse;
//    }
//
//    /**
//     * Create security response
//     */
//    private Map<String, Object> createSecurityResponse(DeviceTrackingResult trackingResult) {
//        Map<String, Object> securityResponse = new HashMap<>();
//        securityResponse.put("isNewDevice", trackingResult.isNewDevice());
//        securityResponse.put("isHighRisk", trackingResult.isHighRisk());
//        securityResponse.put("requiresVerification", trackingResult.requiresVerification());
//        securityResponse.put("securityRecommendations", generateSecurityRecommendations(trackingResult));
//        return securityResponse;
//    }
//
//    /**
//     * Generate security recommendations
//     */
//    private List<String> generateSecurityRecommendations(DeviceTrackingResult trackingResult) {
//        List<String> recommendations = new ArrayList<>();
//
//        if (trackingResult.isNewDevice()) {
//            recommendations.add("This is a new device. Please verify your identity.");
//        }
//
//        if (trackingResult.isHighRisk()) {
//            recommendations.add("High-risk device detected. Additional security measures may be required.");
//        }
//
//        if (trackingResult.requiresVerification()) {
//            recommendations.add("Device verification is required for enhanced security.");
//        }
//
//        return recommendations;
//    }
//
//    /**
//     * Calculate user security score
//     */
//    private int calculateUserSecurityScore(User user, DeviceAnalytics analytics) {
//        int score = 50; // Base score
//
//        // Device trust score
//        score += (int) (analytics.getAverageTrustScore() * 0.3);
//
//        // Risk score penalty
//        score -= (int) (analytics.getAverageRiskScore() * 0.2);
//
//        // Security events penalty
//        score -= analytics.getHighRiskEvents() * 5;
//
//        // Unresolved events penalty
//        score -= analytics.getUnresolvedEvents() * 3;
//
//        return Math.max(0, Math.min(100, score));
//    }
//
//    // Device detection methods (same as in filter)
//    private String generateDeviceFingerprint(HttpServletRequest request) {
//        StringBuilder fingerprint = new StringBuilder();
//
//        String userAgent = request.getHeader("User-Agent");
//        if (userAgent != null) {
//            fingerprint.append(userAgent.hashCode());
//        }
//
//        String accept = request.getHeader("Accept");
//        if (accept != null) {
//            fingerprint.append(accept.hashCode());
//        }
//
//        String acceptLanguage = request.getHeader("Accept-Language");
//        if (acceptLanguage != null) {
//            fingerprint.append(acceptLanguage.hashCode());
//        }
//
//        return String.valueOf(fingerprint.toString().hashCode());
//    }
//
//    private DeviceType determineDeviceType(String userAgent) {
//        if (userAgent == null) return DeviceType.UNKNOWN;
//
//        String ua = userAgent.toLowerCase();
//        if (ua.contains("android")) {
//            return ua.contains("tablet") ? DeviceType.ANDROID_TABLET : DeviceType.ANDROID_PHONE;
//        }
//        if (ua.contains("iphone")) return DeviceType.IPHONE;
//        if (ua.contains("ipad")) return DeviceType.IPAD;
//        if (ua.contains("windows")) return DeviceType.WINDOWS_DESKTOP;
//        if (ua.contains("mac")) return DeviceType.MAC_DESKTOP;
//        if (ua.contains("linux")) return DeviceType.LINUX_DESKTOP;
//
//        return DeviceType.UNKNOWN;
//    }
//
//    private String extractOperatingSystem(String userAgent) {
//        if (userAgent == null) return "Unknown";
//
//        String ua = userAgent.toLowerCase();
//        if (ua.contains("windows nt")) return "Windows";
//        if (ua.contains("mac os x")) return "macOS";
//        if (ua.contains("linux")) return "Linux";
//        if (ua.contains("android")) return "Android";
//        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
//
//        return "Unknown";
//    }
//
//    private String extractBrowser(String userAgent) {
//        if (userAgent == null) return "Unknown";
//
//        String ua = userAgent.toLowerCase();
//        if (ua.contains("chrome")) return "Chrome";
//        if (ua.contains("firefox")) return "Firefox";
//        if (ua.contains("safari") && !ua.contains("chrome")) return "Safari";
//        if (ua.contains("edge")) return "Edge";
//        if (ua.contains("opera")) return "Opera";
//
//        return "Unknown";
//    }
//
//    private boolean detectVpn(HttpServletRequest request) {
//        return request.getHeader("X-Forwarded-For") != null ||
//               request.getHeader("X-Real-IP") != null ||
//               request.getHeader("CF-Connecting-IP") != null;
//    }
//
//    private boolean detectProxy(HttpServletRequest request) {
//        return request.getHeader("Via") != null ||
//               request.getHeader("X-Forwarded-For") != null;
//    }
//
//    private boolean detectTor(HttpServletRequest request) {
//        String userAgent = request.getHeader("User-Agent");
//        return userAgent != null && userAgent.toLowerCase().contains("tor");
//    }
//
//    private boolean detectDataCenter(String ipAddress) {
//        // Simplified detection
//        return false;
//    }
//
//    private boolean detectMobile(String userAgent) {
//        if (userAgent == null) return false;
//        String ua = userAgent.toLowerCase();
//        return ua.contains("mobile") || ua.contains("android") || ua.contains("iphone") || ua.contains("ipad");
//    }
//
//    private String extractSessionId(HttpServletRequest request) {
//        // Extract session ID from JWT token or session
//        return null; // Implement based on your session management
//    }
//
//    private User processUserRegistration(EnhancedRegistrationRequest request) {
//        // Implement user registration logic
//        // This would create a new user and return it
//        return null; // Placeholder
//    }
//
//    // Inner classes
//    @lombok.Data
//    @lombok.Builder
//    private static class DeviceInfo {
//        private String deviceFingerprint;
//        private String userAgent;
//        private String ipAddress;
//        private DeviceType deviceType;
//        private String operatingSystem;
//        private String browser;
//        private boolean isVpn;
//        private boolean isProxy;
//        private boolean isTor;
//        private boolean isDataCenter;
//        private boolean isMobile;
//    }
//
//    @lombok.Data
//    @lombok.Builder
//    private static class DeviceTrackingResult {
//        private Device device;
//        private DeviceSession session;
//        private boolean isNewDevice;
//        private boolean isHighRisk;
//        private boolean requiresVerification;
//    }
//
//    @lombok.Data
//    @lombok.Builder
//    private static class EnhancedRegistrationRequest {
//        private String username;
//        private String email;
//        private String password;
//        private String firstName;
//        private String lastName;
//        // Add other registration fields as needed
//    }
//}