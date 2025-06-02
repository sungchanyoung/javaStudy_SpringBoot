package com.study.ecommerce.domain.product.dto.req;

public record ProductUpdateRequest(
        String name,
        String description,
        Long price,
        Integer stockQuantity,
        Long categoryId
) {
}
