package com.example.cafequeue.order.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID orderId,
        String ticketNumber,
        String orderType,
        LocalDateTime eventTimestamp,
        List<Item> items
) {
    public record Item(String menuItemName, Integer quantity) {}
}
