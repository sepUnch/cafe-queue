package com.example.cafequeue.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
    String customerName,
    String orderType,
    java.time.LocalDateTime pickupTime,
    List<OrderItemRequest> items
) {
    public record OrderItemRequest(
        String menuItemName,
        Integer quantity,
        BigDecimal price
    ) {}
}
