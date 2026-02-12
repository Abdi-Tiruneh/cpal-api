package com.commercepal.apiservice.promotions.affiliate.user.dto;

import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull Boolean isActive)
{
}
