package com.commercepal.apiservice.promotions.affiliate.user;

import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.promotions.affiliate.commission.Commission;
import com.commercepal.apiservice.promotions.affiliate.user.dto.AffiliateAddRequest;
import com.commercepal.apiservice.promotions.affiliate.user.dto.AffiliateFromCustomerRequest;
import com.commercepal.apiservice.promotions.affiliate.user.dto.AffiliatePortalConfigRequest;
import com.commercepal.apiservice.users.credential.CredentialService;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.users.customer.CustomerService;
import com.commercepal.apiservice.users.customer.dto.CustomerRegistrationRequest;
import com.commercepal.apiservice.utils.ConditionalUpdateUtils;
import com.commercepal.apiservice.utils.PhoneValidationUtil;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateService
{

    private static final int CODE_LENGTH = 8;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final AffiliateRepository affiliateRepository;
    private final CredentialService credentialService;
    private final CustomerService customerService;

    /**
     * Registers a new affiliate from an external request.
     */
    public Affiliate registerNewAffiliate(AffiliateAddRequest request)
    {
        String email = request.email().trim().toLowerCase();
        String phone = PhoneValidationUtil.normalizePhoneNumber(request.phoneNumber().trim(), SupportedCountry.ETHIOPIA);

        // Check if a customer with the same email or phone already exists
        if (credentialService.existsByEmail(email)) {
            throw new IllegalStateException(
                    "A customer account with the provided email already exists. " + "Please log in to your existing CommercePal account and convert it to an affiliate profile.");
        }

        if (credentialService.existsByPhone(phone)) {
            throw new IllegalStateException(
                    "A customer account with the provided phone already exists. " + "Please log in to your existing CommercePal account and convert it to an affiliate profile.");
        }

        validateRegistrationRequest(email, phone, null);

        String referralCode = resolveReferralCode(request.referralCode());

        // Register customer account
        customerService.registerCustomer(toCustomerRequest(request));

        return saveAffiliate(referralCode, email, phone, request.firstName(), request.lastName(),
                request.commissionType());
    }

    /**
     * Creates an affiliate from an existing customer profile.
     */
    public Affiliate createFromCustomerProfile(@Valid AffiliateFromCustomerRequest request, Customer customer)
    {
        String email = customer.getCredential().getEmailAddress().trim().toLowerCase();
        String phone = PhoneValidationUtil.normalizePhoneNumber(customer.getCredential().getPhoneNumber().trim(), SupportedCountry.ETHIOPIA);

        validateRegistrationRequest(email, phone, null);

        String referralCode = resolveReferralCode(request.referralCode());

        return saveAffiliate(referralCode, email, phone, customer.getFirstName(), customer.getLastName(),
                request.commissionType());
    }

    /**
     * Configures an affiliate's portal settings.
     */
    public Affiliate configureAffiliateRequest(Long id, AffiliatePortalConfigRequest request)
    {
        Affiliate affiliate = getById(id);
        validatePortalConfigRequest(request);

        boolean isUpdated = false;

        if (request.referralCode() != null && !request.referralCode().isBlank()) {
            String referralCode = request.referralCode().trim().toUpperCase();
            validateRequestedCode(referralCode, id);
            affiliate.setReferralCode(referralCode);
            isUpdated = true;
        }

        isUpdated |= ConditionalUpdateUtils.updateIfChanged(affiliate::setCommissionType, request.commissionType(),
                affiliate.getCommissionType());
        isUpdated |= ConditionalUpdateUtils.updateIfChanged(affiliate::setCommissionRate, request.commissionRate(),
                affiliate.getCommissionRate());

        if (isUpdated) {
            affiliate.setUpdatedAt(Timestamp.from(Instant.now()));
            affiliateRepository.save(affiliate);
        }
        return affiliate;
    }

    /**
     * Updates the active status of an affiliate.
     *
     * @param id     the ID of the affiliate to update
     * @param active the new active status
     * @return the updated affiliate entity
     * @throws IllegalStateException if the affiliate is not properly configured
     */
    @Transactional
    public Affiliate updateStatus(Long id, Boolean active) {
        Affiliate affiliate = getById(id);

        // Validate configuration before allowing activation/deactivation
        if (affiliate.getCommissionType() == null || affiliate.getCommissionRate() == null) {
            throw new IllegalStateException("Affiliate has not been fully configured");
        }

        // Update only if the status has changed
        boolean isUpdated = ConditionalUpdateUtils.updateIfChanged(affiliate::setIsActive, active, affiliate.getIsActive());
        if (isUpdated) {
            affiliate.setUpdatedAt(Timestamp.from(Instant.now()));
            affiliateRepository.save(affiliate);
        }

        return affiliate;
    }


    /**
     * Retrieves an affiliate by ID.
     */
    public Affiliate getById(Long id)
    {
        return affiliateRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affiliate not found with id: " + id));
    }

    /**
     * Retrieves all affiliates ordered by name.
     */
    public List<Affiliate> getAllAffiliates(Sort sort, Boolean isActive)
    {
        if (isActive != null) {
            return affiliateRepository.findByIsActive(isActive, sort);
        }
        return affiliateRepository.findAll(sort);
    }

        /* ======================
       PRIVATE HELPER METHODS
       ====================== */

    private String resolveReferralCode(String providedCode)
    {
        if (providedCode == null || providedCode.isBlank()) {
            return generateUniqueCodeOnly();
        }
        String code = providedCode.trim().toUpperCase();
        validateRequestedCode(code, null);
        return code;
    }

    private Affiliate saveAffiliate(
            String code, String email, String phone, String firstName, String lastName,
            Commission type
    )
    {
        Timestamp now = Timestamp.from(Instant.now());

        Affiliate affiliate = new Affiliate();
        affiliate.setReferralCode(code);
        affiliate.setFirstName(firstName.trim());
        affiliate.setLastName(lastName.trim());
        affiliate.setEmail(email);
        affiliate.setPhoneNumber(phone);
        affiliate.setCommissionType(type);
        affiliate.setIsActive(Boolean.FALSE);
        affiliate.setCreatedAt(now);
        affiliate.setUpdatedAt(now);

        Affiliate saved = affiliateRepository.save(affiliate);
        log.info("New affiliate registered | AffiliateService | saveAffiliate | id={}, referralCode={}, email={}",
                saved.getId(), affiliate.getReferralCode(), saved.getEmail());
        return saved;
    }

    private CustomerRegistrationRequest toCustomerRequest(AffiliateAddRequest request)
    {
        return CustomerRegistrationRequest
                .builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .emailAddress(request.email())
                .password(request.password())
                .confirmPassword(request.confirmPassword())
                .country(request.country())
//                .countryCode(request.countryCode())
                .phoneNumber(request.phoneNumber())
                .registrationChannel(request.registrationChannel())
//                .deviceId(request.deviceId())
                .build();
    }

    /**
     * Validates registration request for uniqueness of email and phone number.
     */
    private void validateRegistrationRequest(String email, String formattedPhoneNumber, Long currentId)
    {
        // email check
        affiliateRepository.findByEmail(email).ifPresent(a -> {
            if (!a.getId().equals(currentId)) {
                throw new IllegalArgumentException("Email already in use: " + email);
            }
        });
        // phone check
        if (formattedPhoneNumber != null) {
            affiliateRepository.findByPhoneNumber(formattedPhoneNumber).ifPresent(a -> {
                if (!a.getId().equals(currentId)) {
                    throw new IllegalArgumentException("Phone number already in use: " + formattedPhoneNumber);
                }
            });
        }
    }

    /**
     * Validates a requested referral code for format and uniqueness.
     */
    public void validateRequestedCode(String requestedCode, Long affiliateId)
    {
        String normalized = requestedCode == null ? null : requestedCode.trim().toUpperCase();

        if (normalized == null || normalized.isEmpty()) {
            throw new IllegalArgumentException("Requested code cannot be empty");
        }

        if (!normalized.matches("^[A-Z0-9]{4,8}$")) {
            throw new IllegalArgumentException("Requested code must be 4-8 alphanumeric characters (A-Z0-9)");
        }

        boolean exists;
        if (affiliateId == null) {
            // New affiliate: check any match
            exists = affiliateRepository.existsByReferralCode(normalized);
        } else {
            // Existing affiliate: check for matches excluding self
            exists = affiliateRepository.existsByReferralCodeAndIdNot(normalized, affiliateId);
        }

        if (exists) {
            throw new IllegalArgumentException("Requested referral code already in use: " + normalized);
        }
    }


    /**
     * Validates portal config request for commission constraints.
     */
    private void validatePortalConfigRequest(AffiliatePortalConfigRequest request)
    {
        if (request.commissionType() == Commission.PERCENTAGE) {
            if (request.commissionRate() == null) {
                throw new IllegalArgumentException("Commission rate is required.");
            }
            if (request.commissionRate().compareTo(BigDecimal.ZERO) < 0 || request
                    .commissionRate()
                    .compareTo(BigDecimal.valueOf(100)) > 0)
            {
                throw new IllegalArgumentException("Percentage commission must be between 0 and 100");
            }
        }
    }

    /**
     * Generates a unique referral code.
     */
    private String generateUniqueCodeOnly()
    {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = ReferralCodeGenerator.randomAlphaNumeric(CODE_LENGTH);
            if (!affiliateRepository.existsByReferralCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Unable to generate unique referral code after " + MAX_GENERATION_ATTEMPTS + " tries");
    }
}
