package com.commercepal.apiservice.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Request object for paginated and sorted queries. Provides a clean API-level abstraction over
 * Pageable.
 */
@Data
@ParameterObject
public class PageRequestDto {

  @Schema(description = "Page number (0-based)", example = "0")
  private final int page = 0;

  @Schema(description = "Number of records per page", example = "10")
  private final int size = 10;

  @Schema(description = "Sort by field name", example = "createdAt")
  private final String sortBy = "createdAt";

  @Schema(description = "Sort direction: ASC or DESC", example = "DESC")
  private final String direction = "DESC";

  public Pageable toPageable() {
    Sort sort = Sort.by(
        "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
        sortBy
    );
    return PageRequest.of(page, size, sort);
  }
}
