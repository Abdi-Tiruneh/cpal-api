package com.commercepal.apiservice.shared.health;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple CORS test endpoint
 */
@RestController
@RequestMapping("/api/v1/health")
public class CorsTestController {

  @GetMapping("/cors-test")
  public ResponseEntity<Map<String, Object>> corsTest() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "success");
    response.put("message", "CORS is working correctly!");
    response.put("timestamp", LocalDateTime.now());
    response.put("corsEnabled", true);
    return ResponseEntity.ok(response);
  }
}
