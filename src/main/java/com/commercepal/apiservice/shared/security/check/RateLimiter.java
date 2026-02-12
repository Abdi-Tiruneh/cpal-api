package com.commercepal.apiservice.shared.security.check;//package com.fastpay.agent.shared.security;
//
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap; /**
// * Basic in-memory rate limiter for login attempts.
// */
//@Component
//public class RateLimiter {
//    private static final int MAX_ATTEMPTS = 5;
//    private static final long WINDOW_SECONDS = 60;
//
//    private final Map<String, List<Long>> attempts = new ConcurrentHashMap<>();
//
//    public boolean allowRequest(String username) {
//        long now = System.currentTimeMillis();
//        List<Long> userAttempts = attempts.computeIfAbsent(username, k -> new ArrayList<>());
//
//        synchronized (userAttempts) {
//            // Remove attempts older than the window
//            userAttempts.removeIf(timestamp -> now - timestamp > WINDOW_SECONDS * 1000);
//
//            if (userAttempts.size() >= MAX_ATTEMPTS) {
//                return false;
//            }
//
//            userAttempts.add(now);
//            return true;
//        }
//    }
//}
