
package com.example.cafequeue.order.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID orderId,
        String ticketNumber,
        String previousStatus,
        String newStatus,
        LocalDateTime eventTimestamp,
        LocalDateTime pickupTime
) {}
