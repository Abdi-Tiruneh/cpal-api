package com.commercepal.apiservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds app.init.* from application.yml (env placeholders resolved by Spring).
 * All values are read from application.yml; see "app.init" section there.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.init")
public class AppInitProperties {

  /** app.init.enabled */
  private boolean enabled;

  /** app.init.customer-default-password (for customer seed) */
  private String customerDefaultPassword;

  /** app.init.create-all-roles */
  private boolean createAllRoles;

  /** app.init.super-admin.* */
  private SuperAdmin superAdmin = new SuperAdmin();

  @Data
  public static class SuperAdmin {
    /** app.init.super-admin.email */
    private String email;
    /** app.init.super-admin.phone */
    private String phone;
    /** app.init.super-admin.employee-id */
    private String employeeId;
    /** app.init.super-admin.default-password (for super admin and staff init) */
    private String defaultPassword;
  }
}
