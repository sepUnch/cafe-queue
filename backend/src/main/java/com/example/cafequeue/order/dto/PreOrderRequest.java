package com.example.cafequeue.order.dto;

import com.example.cafequeue.order.OrderRequest;
import java.time.LocalDateTime;
import java.util.List;

public record PreOrderRequest(
        String customerName,
        List<OrderRequest.OrderItemRequest> items,
        LocalDateTime pickupTime
) {}
