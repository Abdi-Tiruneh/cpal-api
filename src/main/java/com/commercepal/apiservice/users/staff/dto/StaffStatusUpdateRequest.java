package com.commercepal.apiservice.users.staff.dto;

import com.commercepal.apiservice.users.staff.enums.StaffStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request to update staff employment status only.
 */
@Schema(description = "Update employment status. For TERMINATED, optionally set terminationDate and reason.")
public record StaffStatusUpdateRequest(
    @Schema(description = "New employment status", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status is required")
    StaffStatus status,

    @Schema(description = "Termination date (when status is TERMINATED)", example = "2024-12-31")
    LocalDate terminationDate,

    @Schema(description = "Optional reason or notes (e.g. termination reason)")
    String reason
) {}
