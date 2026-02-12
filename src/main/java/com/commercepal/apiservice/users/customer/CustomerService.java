package com.commercepal.apiservice.users.customer;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.credential.CredentialRepository;
import com.commercepal.apiservice.users.enums.UserType;
import com.commercepal.apiservice.users.customer.dto.CustomerRegistrationRequest;
import com.commercepal.apiservice.users.customer.dto.CustomerResponse;
import com.commercepal.apiservice.users.customer.dto.CustomerUpdateRequest;
import com.commercepal.apiservice.users.enums.IdentityProvider;
import com.commercepal.apiservice.users.enums.UserStatus;
import com.commercepal.apiservice.users.referral.ReferralCodeUtils;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.role.RoleDefinition;
import com.commercepal.apiservice.users.role.RoleDefinitionRepository;
import com.commercepal.apiservice.users.till.TillSequenceService;
import com.commercepal.apiservice.utils.MapperUtils;
import com.commercepal.apiservice.utils.PhoneValidationUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for customer operations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final CredentialRepository credentialRepository;
  private final RoleDefinitionRepository roleDefinitionRepository;
  private final PasswordEncoder passwordEncoder;
  private final TillSequenceService tillSequenceService;
  private final ReferralCodeUtils referralCodeUtils;

  /**
   * Register a new customer.
   *
   * @param request the registration request
   */
  public void registerCustomer(CustomerRegistrationRequest request) {
    log.info("Registering new customer with email: {}, phone: {}, country: {}",
        request.emailAddress(), request.phoneNumber(), request.country());

    String email = request.emailAddress() != null ? request.emailAddress().trim() : null;
    String phoneNumber = request.phoneNumber() != null ? request.phoneNumber().trim() : null;

    if (request.password() != null && request.confirmPassword() != null
        && !request.password().equals(request.confirmPassword())) {
      throw new IllegalArgumentException("Password and confirm password do not match");
    }

    PhoneValidationUtil.validateCountrySpecificIdentifiers(email, phoneNumber, request.country());

    if (phoneNumber != null && !phoneNumber.isBlank()) {
      PhoneValidationUtil.validatePhoneNumberLength(phoneNumber, request.country());
    }

    validateUniqueIdentifiers(email, phoneNumber);

    String accountNumber = tillSequenceService.generateAccountNumber(UserType.CUSTOMER);
    String commissionAccount = accountNumber + "1";

    String encodedPassword = passwordEncoder.encode(request.password());

    RoleDefinition customerRole = roleDefinitionRepository.findByCode(RoleCode.ROLE_CUSTOMER)
        .orElseThrow(() -> new IllegalStateException("ROLE_CUSTOMER not found in system"));

    // Create and save the Credential first (required for Customer's non-nullable credential_id)
    Credential credential = Credential.builder()
        .userType(UserType.CUSTOMER)
        .emailAddress(email)
        .phoneNumber(phoneNumber)
        .passwordHash(encodedPassword)
        .status(UserStatus.ACTIVE)
        .deleted(false)
        .version(0L)
        .createdAt(LocalDateTime.now())
        .createdBy("SYSTEM")
        .identityProvider(IdentityProvider.LOCAL)
        .build();

    credential.assignRole(customerRole);

    Credential savedCredential = credentialRepository.save(credential);
    log.debug("Account credential created | credentialId={}", savedCredential.getId());

    // Now create the Customer with the credential already linked
    String referralCode = referralCodeUtils.generateCustomerReferralCode();
    Customer customer = Customer.builder()
        .oldCustomerId(0L)
        .accountNumber(accountNumber)
        .commissionAccount(commissionAccount)
        .firstName(request.firstName())
        .lastName(request.lastName())
        .country(request.country().getCode())
        .preferredLanguage("en")
        .preferredCurrency(SupportedCurrency.ETB)
        .referralCode(referralCode)
        .registrationChannel(request.registrationChannel())
        .createdAt(LocalDateTime.now())
        .createdBy("SYSTEM")
        .isDeleted(false)
        .version(0L)
        .credential(savedCredential)
        .build();

    Customer savedCustomer = customerRepository.save(customer);
    log.info("Customer registered successfully | customerId={} | accountNumber={}",
        savedCustomer.getId(), savedCustomer.getAccountNumber());
  }

  /**
   * Update customer information.
   *
   * @param customerId the customer ID
   * @param request    the update request
   * @return the updated customer response
   */
  public CustomerResponse updateCustomer(Long customerId, CustomerUpdateRequest request) {
    log.info("Updating customer with ID: {}", customerId);

    // Fetch customer with credential eagerly loaded to prevent LazyInitializationException
    Customer customer = customerRepository.findByIdWithCredential(customerId).orElseThrow(
        () -> new IllegalArgumentException("OldCustomer not found with ID: " + customerId));
    MapperUtils.applyIfNotBlank(request.firstName(), customer::setFirstName);
    MapperUtils.applyIfNotBlank(request.lastName(), customer::setLastName);
    MapperUtils.applyIfNotNull(request.country().getCode(), customer::setCountry);
    MapperUtils.applyIfNotBlank(request.city(), customer::setCity);
    MapperUtils.applyIfNotBlank(request.stateProvince(), customer::setStateProvince);
    MapperUtils.applyIfNotBlank(request.preferredLanguage(), customer::setPreferredLanguage);
    MapperUtils.applyIfNotNull(request.preferredCurrency(), customer::setPreferredCurrency);
    MapperUtils.applyIfNotBlank(request.customerNotes(), customer::setCustomerNotes);

    Customer updatedCustomer = customerRepository.save(customer);
    log.info("Customer updated successfully | customerId={}", customerId);

    return CustomerResponse.from(updatedCustomer);
  }

  /**
   * Get all customers (for admin).
   *
   * @param pageable pagination information
   * @return page of customers
   */
  @Transactional(readOnly = true)
  public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
    log.debug("Fetching all customers with pagination: {}", pageable);
    Page<Customer> customers = customerRepository.findAll(pageable);
    return customers.map(CustomerResponse::from);
  }

  /**
   * Get customer by ID.
   *
   * @param customerId the customer ID
   * @return the customer response
   */
  @Transactional(readOnly = true)
  public Optional<CustomerResponse> getCustomerById(Long customerId) {
    log.debug("Fetching customer with ID: {}", customerId);
    return customerRepository.findByIdWithCredential(customerId).map(CustomerResponse::from);
  }


  /**
   * Validate that email and phone are unique.
   */
  private void validateUniqueIdentifiers(String emailAddress, String phoneNumber) {
    if (emailAddress != null && !emailAddress.isBlank()) {
      if (credentialRepository.existsByEmailAddress(emailAddress)) {
        throw new IllegalArgumentException("Email address already exists");
      }
    }
    if (phoneNumber != null && !phoneNumber.isBlank()) {
      if (credentialRepository.existsByPhoneNumber(phoneNumber)) {
        throw new IllegalArgumentException("Phone number already exists");
      }
    }
  }


}
