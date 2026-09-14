package com.example.cafequeue.order;

import com.example.cafequeue.order.event.OrderEventProducer;
import com.example.cafequeue.order.event.OrderPlacedEvent;
import com.example.cafequeue.order.event.OrderStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private QueueTicketRepository queueTicketRepository;

    @Mock
    private QueueTicketHistoryRepository queueTicketHistoryRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_shouldCalculateTotalPriceCorrectlyAndPublishEvents() {
        OrderRequest request = new OrderRequest("Budi", "DINE_IN", null, List.of(
                new OrderRequest.OrderItemRequest("Kopi", 2, new BigDecimal("18000")),
                new OrderRequest.OrderItemRequest("Roti", 1, new BigDecimal("15000"))
        ));

        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setCustomerName("Budi");
        savedOrder.setOrderType("DINE_IN");
        savedOrder.setStatus("QUEUED");
        savedOrder.setTotalPrice(new BigDecimal("51000"));

        OrderItem savedItem = new OrderItem();
        savedItem.setId(UUID.randomUUID());

        QueueTicket savedTicket = new QueueTicket();
        savedTicket.setId(UUID.randomUUID());
        savedTicket.setTicketNumber(12);
        savedTicket.setStatus("QUEUED");
        savedTicket.setStatusTimestamp(LocalDateTime.now());

        QueueTicketHistory savedHistory = new QueueTicketHistory();
        savedHistory.setId(UUID.randomUUID());

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(savedOrder.getId());
            return Mono.just(order);
        });

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(savedItem));
        when(queueTicketRepository.getNextTicketNumberForToday()).thenReturn(Mono.just(12));
        when(queueTicketRepository.save(any(QueueTicket.class))).thenReturn(Mono.just(savedTicket));
        when(queueTicketHistoryRepository.save(any(QueueTicketHistory.class))).thenReturn(Mono.just(savedHistory));
        
        when(orderEventProducer.publishOrderPlaced(any())).thenReturn(Mono.empty());
        when(orderEventProducer.publishOrderStatusChanged(any())).thenReturn(Mono.empty());

        Mono<OrderResponse> responseMono = orderService.createOrder(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.totalPrice().compareTo(new BigDecimal("51000")) == 0)
                .verifyComplete();
                
        verify(orderEventProducer, times(1)).publishOrderPlaced(any(OrderPlacedEvent.class));
        verify(orderEventProducer, times(1)).publishOrderStatusChanged(any(OrderStatusChangedEvent.class));
    }

    @Test
    void cancelOrder_shouldFail_ifStatusIsNotQueued() {
        UUID orderId = UUID.randomUUID();
        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus("COOKING");

        when(orderRepository.findByIdAndNotDeleted(orderId)).thenReturn(Mono.just(existingOrder));

        StepVerifier.create(orderService.cancelOrder(orderId))
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        throwable.getMessage().contains("Order status mismatch"))
                .verify();
    }

    @Test
    void cancelOrder_shouldSucceed_ifStatusIsQueued() {
        UUID orderId = UUID.randomUUID();
        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus("QUEUED");

        QueueTicket ticket = new QueueTicket();
        ticket.setId(UUID.randomUUID());
        ticket.setTicketNumber(15);
        ticket.setStatus("QUEUED");

        when(orderRepository.findByIdAndNotDeleted(orderId)).thenReturn(Mono.just(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(existingOrder));
        when(queueTicketRepository.findByOrderIdAndNotDeleted(orderId)).thenReturn(Mono.just(ticket));
        when(queueTicketRepository.save(any(QueueTicket.class))).thenReturn(Mono.just(ticket));
        when(queueTicketHistoryRepository.save(any(QueueTicketHistory.class))).thenReturn(Mono.just(new QueueTicketHistory()));
        
        when(orderEventProducer.publishOrderStatusChanged(any())).thenReturn(Mono.empty());

        StepVerifier.create(orderService.cancelOrder(orderId))
                .verifyComplete();
                
        verify(orderEventProducer, times(1)).publishOrderStatusChanged(any(OrderStatusChangedEvent.class));
    }
}
