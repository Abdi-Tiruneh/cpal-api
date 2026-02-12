package com.commercepal.apiservice.users.customer.address;

import com.commercepal.apiservice.users.customer.address.dto.AddressRequest;
import com.commercepal.apiservice.users.customer.address.dto.AddressResponse;
import com.commercepal.apiservice.users.customer.address.migration.AddressMigrationService;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * CustomerAddressController
 * <p>
 * Customer-address API tailored for international e-commerce requirements: - Supports normalized
 * address fields (state/county/city/district/street) - Enforces exactly one default address per
 * customer - Provides clear lifecycle endpoints for create/update/default/delete
 */
@RestController
@RequestMapping("/api/v1/customers/addresses")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Customer Addresses", description = "Secure customer address management APIs (customer role only).")
public class CustomerAddressController {

  private final CustomerAddressService customerAddressService;
  private final AddressMigrationService addressMigrationService;

  @Operation(summary = "List customer addresses", description = "Returns all saved addresses for the authenticated customer. Requires bearer token with customer role.", responses = {
      @ApiResponse(responseCode = "200", description = "Addresses retrieved successfully", content = @Content(schema = @Schema(implementation = AddressResponse.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  @GetMapping
  public ResponseEntity<ResponseWrapper<Iterable<AddressResponse>>> list() {
    return ResponseWrapper.success(customerAddressService.getMyAddresses());
  }

  @Operation(summary = "Create a new address", description = "Adds a new address for the authenticated customer. If marked default or first address, it becomes the default.", responses = {
      @ApiResponse(responseCode = "201", description = "Address created", content = @Content(schema = @Schema(implementation = AddressResponse.class))),
      @ApiResponse(responseCode = "400", description = "Validation error"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ResponseWrapper<AddressResponse>> create(
      @Valid @RequestBody AddressRequest request) {
    return ResponseWrapper.created(customerAddressService.createAddress(request));
  }

  @PutMapping("/{addressId}")
  @Operation(summary = "Update an address")
  public ResponseEntity<ResponseWrapper<AddressResponse>> update(
      @PathVariable Long addressId,
      @RequestBody AddressRequest request) {
    return ResponseWrapper.success(customerAddressService.updateAddress(addressId, request));
  }

  @PatchMapping("/{addressId}/default")
  @Operation(summary = "Set default address")
  public ResponseEntity<ResponseWrapper<AddressResponse>> makeDefault(
      @PathVariable Long addressId) {
    return ResponseWrapper.success(customerAddressService.setDefault(addressId));
  }

  @Operation(summary = "Delete an address", description = "Deletes an address for the authenticated customer. If default is removed, another address is promoted automatically.", responses = {
      @ApiResponse(responseCode = "204", description = "Address deleted"),
      @ApiResponse(responseCode = "404", description = "Address not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  @DeleteMapping("/{addressId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<ResponseWrapper<String>> delete(@PathVariable Long addressId) {
    customerAddressService.deleteAddress(addressId);
    return ResponseWrapper.success("Address deleted successfully");
  }

  @Operation(summary = "Migrate addresses", description = "Triggers migration of legacy addresses. Admin only.")
  @PostMapping("/migration")
  public ResponseEntity<ResponseWrapper<String>> migrateAddresses() {
    return ResponseWrapper.success(addressMigrationService.migrateAddresses());
  }
}
