package dev.ankit.platform.product_service.dto.internal;


import java.math.BigDecimal;

public record ProductInternalDto(
        String productId,
        boolean available,
        BigDecimal price,
        Integer stock
) {
}