package com.commercepal.apiservice.promotions.affiliate.user;

import com.commercepal.apiservice.promotions.affiliate.user.dto.AffiliateAddRequest;
import com.commercepal.apiservice.promotions.affiliate.user.dto.AffiliateFromCustomerRequest;
import com.commercepal.apiservice.promotions.affiliate.user.dto.AffiliateResponse;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import com.commercepal.apiservice.utils.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/affiliates")
@RequiredArgsConstructor
public class AffiliateController {

    private final AffiliateService affiliateService;
    private final CurrentUserService currentUserService;

    /**
     * Register a new affiliate without an existing customer profile. Requires full registration
     * data including personal and security details.
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<Affiliate>> registerNewAffiliate(
        @Valid @RequestBody AffiliateAddRequest request
    ) {
        return ResponseWrapper.success(affiliateService.registerNewAffiliate(request));
    }

    /**
     * Create an affiliate from an existing customer profile. Uses minimal affiliate-specific info.
     */
    @PostMapping("/register/from-customer")
    public ResponseEntity<ResponseWrapper<Affiliate>> createAffiliateFromCustomer(
        @Valid @RequestBody AffiliateFromCustomerRequest request) {
        Customer customer = currentUserService.getCurrentCustomer();
        return ResponseWrapper.success(affiliateService.createFromCustomerProfile(request, customer));
    }

    @GetMapping("/my-profile")
    public ResponseEntity<ResponseWrapper<AffiliateResponse>> getMyProfile() {
        Affiliate affiliate = currentUserService.getCurrentAffiliate();
        return ResponseWrapper.success(AffiliateMapper.toResponse(affiliate));
    }
}
