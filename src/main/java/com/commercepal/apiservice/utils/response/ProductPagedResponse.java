package com.commercepal.apiservice.utils.response;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductPagedResponse<T> {

  private PaginationMeta pagination;
  private List<T> items;

  public static <T> ProductPagedResponse<T> from(int page, int size,
      List<T> items) {
    int safePage = Math.max(page, 0);
    List<T> safeItems = items == null ? Collections.emptyList() : items;

    boolean hasNextPage = safeItems.size() <= size;
    boolean hasPreviousPage = safePage > 0;

    return ProductPagedResponse.<T>builder()
        .items(safeItems)
        .pagination(PaginationMeta.builder()
            .page(safePage)
            .size(safeItems.size())
            .hasNext(hasNextPage)
            .hasPrevious(hasPreviousPage)
            .build())
        .build();
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PaginationMeta {

    private int page;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
  }
}
