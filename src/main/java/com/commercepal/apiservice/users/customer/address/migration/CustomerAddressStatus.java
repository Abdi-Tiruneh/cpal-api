package com.commercepal.apiservice.users.customer.address.migration;

public enum CustomerAddressStatus {
  ACTIVE,        // The address is currently usable and shown in lists
  INACTIVE,      // Not shown for selection, but still retained (e.g., customer manually disabled it)
  DELETED,       // Soft deleted - kept in DB but not visible
  PENDING_VALIDATION, // Optional: if you support verifying address location via OTP, GPS, etc.
  INVALID        // Optional: address failed validation or was blacklisted
}
