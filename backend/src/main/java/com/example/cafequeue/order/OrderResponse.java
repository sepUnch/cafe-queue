package com.example.cafequeue.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID orderId,
    Integer ticketNumber,
    String customerName,
    String orderType,
    String status,
    BigDecimal totalPrice,
    LocalDateTime createdDate,
    List<OrderItemResponse> items,
    List<QueueTicketHistoryResponse> history
) {
    public record OrderItemResponse(
        String menuItemName,
        Integer quantity,
        BigDecimal price
    ) {}

    public record QueueTicketHistoryResponse(
        String status,
        LocalDateTime timestamp
    ) {}
}
