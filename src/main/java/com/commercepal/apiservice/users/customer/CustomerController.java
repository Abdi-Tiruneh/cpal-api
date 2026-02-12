package com.commercepal.apiservice.users.customer;

import com.commercepal.apiservice.users.customer.dto.CustomerRegistrationRequest;
import com.commercepal.apiservice.users.customer.dto.CustomerResponse;
import com.commercepal.apiservice.users.customer.dto.CustomerUpdateRequest;
import com.commercepal.apiservice.utils.CurrentUserService;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for customer operations.
 * <p>
 * Provides public endpoints for customer registration and authenticated endpoints for profile
 * management. Supports:
 * <ul>
 * <li>Public customer registration</li>
 * <li>Authenticated profile retrieval and updates</li>
 * <li>Customer self-service operations</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer", description = """
    Customer-facing APIs for registration and profile management.
    
    **Public Endpoints:**
    - Customer registration (no authentication required)
    
    **Authenticated Endpoints:**
    - Get own profile
    - Update own profile (PUT/PATCH)
    
    All authenticated endpoints require a valid JWT token with ROLE_CUSTOMER.
    """)
public class CustomerController {

  private final CustomerService customerService;
  private final CurrentUserService currentUserService;

  // PUBLIC ENDPOINTS

  /**
   * Register a new customer.
   * <p>
   * Public endpoint - no authentication required. Creates a new customer account with the provided
   * information.
   *
   * @param request the registration request containing customer details
   * @return the registration response with account details
   */
  @PostMapping("/register")
  @Operation(summary = "Register a new customer", description = """
      Creates a new customer account in the system.
      
      **Public endpoint - no authentication required.**
      
      **Country-specific validation:**
      - Ethiopia (ET): Phone number is required, email is optional
      - Other countries: Email is required, phone number is optional
      
      **Password requirements:**
      - Minimum 6 characters
      - Maximum 128 characters
      - Password and confirm password must match
      
      **Returns:**
      - Customer ID and account number
      - Referral code for referral programs
      - Registration timestamp
      """)
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Customer registered successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request - validation failed or duplicate email/phone"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponseWrapper<String>> registerCustomer(
      @Valid @RequestBody CustomerRegistrationRequest request) {

    log.info("[CUSTOMER-API] POST /register - email: {}, phone: {}, country: {}",
        request.emailAddress(),
        request.phoneNumber(),
        request.country());

    customerService.registerCustomer(request);

    log.info("[CUSTOMER-API] Customer registered successfully - email: {}, phone: {}",
        request.emailAddress(), request.phoneNumber());

    return ResponseWrapper.created("Customer registered successfully");
  }

  // AUTHENTICATED ENDPOINTS

  /**
   * Get the current authenticated customer's profile using their token.
   *
   * @return the customer's profile information
   */
  @GetMapping("/me")
  @Transactional(readOnly = true)
  @SecurityRequirement(name = "Bearer Authentication")
  @Operation(summary = "Get my profile", description = """
      Retrieves the profile of the currently authenticated customer.
      
      **Requires authentication with ROLE_CUSTOMER.**
      
      Uses the JWT token to identify the customer.
      Convenient endpoint that doesn't require knowing the customer ID.
      """)
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
      @ApiResponse(responseCode = "404", description = "Customer not found")
  })
  public ResponseEntity<ResponseWrapper<CustomerResponse>> getMyProfile() {
    Customer customer = currentUserService.getCurrentCustomer();
    log.info("[CUSTOMER-API] Profile retrieved for user: {}", customer.getAccountNumber());
    return ResponseWrapper.success("Profile retrieved successfully",
        CustomerResponse.from(customer));
  }

  /**
   * Update my profile using the authenticated user's token.
   *
   * @param request the update request
   * @return the updated customer profile
   */
  @PutMapping("/me")
  @SecurityRequirement(name = "Bearer Authentication")
  @Operation(summary = "Update my profile", description = """
      Updates the profile of the currently authenticated customer.
      
      **Requires authentication with ROLE_CUSTOMER.**
      
      Uses the JWT token to identify the customer.
      """)
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Customer not found")
  })
  public ResponseEntity<ResponseWrapper<CustomerResponse>> updateMyProfile(
      @Valid @RequestBody CustomerUpdateRequest request) {

    Customer customer = currentUserService.getCurrentCustomer();
    log.info("[CUSTOMER-API] PUT /me - user: {}", customer.getAccountNumber());

    try {
      CustomerResponse response = customerService.updateCustomer(customer.getId(), request);
      log.info("[CUSTOMER-API] Profile updated for user: {}", customer.getAccountNumber());
      return ResponseWrapper.success("Profile updated successfully", response);
    } catch (IllegalArgumentException e) {
      log.warn("[CUSTOMER-API] Update failed for user: {} - {}",
          customer.getAccountNumber(), e.getMessage());
      return ResponseWrapper.notFound(e.getMessage());
    }
  }

}
