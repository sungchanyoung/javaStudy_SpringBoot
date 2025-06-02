package com.study.ecommerce.domain.product.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProductCreateRequest(
        @NotBlank(message = "상품 이름은 필수입니다.")
        String name,
        String description,

        @NotBlank(message = "가격은 필수입니다.")
        @Positive(message = "가격은 0보다 커야합니다.")
        Long price,

        @NotBlank(message = "제고 수량은 필수입니다.")
        @Positive(message = "제고수량은 양수여야합니다.")
        Integer stockQuantity,

        @NotBlank(message = "카테고리는 필수입니다.")
        Long categoryId
        ) {
}
