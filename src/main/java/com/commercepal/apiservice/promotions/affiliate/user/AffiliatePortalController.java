package com.commercepal.apiservice.promotions.affiliate.user;

import com.commercepal.apiservice.promotions.affiliate.user.dto.AffiliatePortalConfigRequest;
import com.commercepal.apiservice.promotions.affiliate.user.dto.StatusUpdateRequest;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portal/affiliates")
@CrossOrigin(origins = {"*"}, maxAge = 3600L)
@RequiredArgsConstructor
public class AffiliatePortalController
{

    private final AffiliateService affiliateService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<Affiliate>>> getAll(
            @RequestParam(defaultValue = "name") @Pattern(regexp = "^(name|date)$", message = "sortBy must be either 'name' or 'date'") String sortBy,
            @RequestParam(defaultValue = "asc") @Pattern(regexp = "^(asc|desc)$", message = "sortDirection must be either 'asc' or 'desc'") String sortDirection,
            @RequestParam(required = false) Boolean isActive
    )
    {
        Sort sort;
        if (sortBy.equalsIgnoreCase("name")) {
            // Sort by firstName then lastName
            sort = Sort
                    .by(Sort.Direction.fromString(sortDirection), "firstName")
                    .and(Sort.by(Sort.Direction.fromString(sortDirection), "lastName"));
        } else {
            // Sort by createdDate
            sort = Sort.by(Sort.Direction.fromString(sortDirection), "updatedAt");
        }

        return ResponseWrapper.success(affiliateService.getAllAffiliates(sort, isActive));
    }

    @PutMapping("/{id}/config")
    public ResponseEntity<ResponseWrapper<Affiliate>> configureAffiliateRequest(
            @PathVariable Long id,
            @RequestBody @Valid AffiliatePortalConfigRequest request
    )
    {
        return ResponseWrapper.success(affiliateService.configureAffiliateRequest(id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseWrapper<Affiliate>> updateAffiliateStatus(
            @PathVariable Long id,
            @RequestBody @Valid StatusUpdateRequest request
    )
    {
        boolean active = request.isActive();

        Affiliate affiliate = affiliateService.updateStatus(id, active);

        String message = active ? "Affiliate has been successfully activated." : "Affiliate has been successfully deactivated.";

        return ResponseWrapper.success(message, affiliate);
    }

}
