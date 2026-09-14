package com.example.cafequeue.board;

import java.time.LocalDateTime;

public record QueueBoardEvent(
        String ticketNumber,
        String status,
        LocalDateTime timestamp,
        LocalDateTime pickupTime
) {}
