package com.commercepal.apiservice.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Product variant/SKU with pricing and availability")
public record ProductVariantView(
    @Schema(description = "Product configuration/variant identifier", example = "I32o646ehujb") String configId,

    @Schema(description = "Available quantity for this variant", example = "88") int quantity,

    @Schema(description = "Sales count for this variant", example = "150") int salesCount,

    @Schema(description = "Variant-specific pricing (if different from base product)") PricingView pricing,

    @Schema(description = "Configurator selections that define this variant (e.g., Color: Black, Size: Large)") List<VariantConfiguratorView> configurators) {

}
