package com.example.cafequeue.order;

import com.example.cafequeue.order.event.OrderEventProducer;
import com.example.cafequeue.order.event.OrderPlacedEvent;
import com.example.cafequeue.order.event.OrderStatusChangedEvent;
import com.example.cafequeue.outbox.OutboxEvent;
import com.example.cafequeue.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final QueueTicketRepository queueTicketRepository;
    private final QueueTicketHistoryRepository queueTicketHistoryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventProducer orderEventProducer;
    private final TransactionalOperator transactionalOperator;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        QueueTicketRepository queueTicketRepository,
                        QueueTicketHistoryRepository queueTicketHistoryRepository,
                        OutboxEventRepository outboxEventRepository,
                        OrderEventProducer orderEventProducer,
                        TransactionalOperator transactionalOperator,
                        ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.queueTicketRepository = queueTicketRepository;
        this.queueTicketHistoryRepository = queueTicketHistoryRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.orderEventProducer = orderEventProducer;
        this.transactionalOperator = transactionalOperator;
        this.objectMapper = objectMapper;
    }

    public Mono<OrderResponse> createOrder(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setOrderType(request.orderType());
        order.setStatus("QUEUED");

        BigDecimal totalPrice = request.items().stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    // Save Order Items
                    Flux<OrderItem> savedItems = Flux.fromIterable(request.items())
                            .map(itemReq -> {
                                OrderItem item = new OrderItem();
                                item.setOrderId(savedOrder.getId());
                                item.setMenuItemName(itemReq.menuItemName());
                                item.setQuantity(itemReq.quantity());
                                item.setPrice(itemReq.price());
                                return item;
                            })
                            .flatMap(orderItemRepository::save);

                    // Generate ticket number and save QueueTicket
                    Mono<QueueTicket> savedTicket = queueTicketRepository.getNextTicketNumberForToday()
                            .flatMap(ticketNumber -> {
                                QueueTicket ticket = new QueueTicket();
                                ticket.setOrderId(savedOrder.getId());
                                ticket.setTicketNumber(ticketNumber);
                                ticket.setStatus("QUEUED");
                                ticket.setStatusTimestamp(LocalDateTime.now());
                                ticket.setPickupTime(request.pickupTime());
                                return queueTicketRepository.save(ticket);
                            });

                    return Mono.zip(savedItems.collectList(), savedTicket)
                            .flatMap(tuple -> {
                                List<OrderItem> items = tuple.getT1();
                                QueueTicket ticket = tuple.getT2();
                                
                                QueueTicketHistory history = new QueueTicketHistory();
                                history.setQueueTicketId(ticket.getId());
                                history.setStatus("QUEUED");
                                history.setStatusTimestamp(ticket.getStatusTimestamp());
                                history.setCreatedDate(LocalDateTime.now());
                                
                                return queueTicketHistoryRepository.save(history)
                                        .flatMap(savedHistory -> {
                                            // Prepare Events
                                            List<OrderPlacedEvent.Item> eventItems = items.stream()
                                                    .map(i -> new OrderPlacedEvent.Item(i.getMenuItemName(), i.getQuantity()))
                                                    .collect(Collectors.toList());
                                                    
                                            OrderPlacedEvent placedEvent = new OrderPlacedEvent(
                                                    savedOrder.getId(),
                                                    String.valueOf(ticket.getTicketNumber()),
                                                    savedOrder.getOrderType(),
                                                    LocalDateTime.now(),
                                                    eventItems
                                            );
                                            
                                            OrderStatusChangedEvent statusEvent = new OrderStatusChangedEvent(
                                                    savedOrder.getId(),
                                                    String.valueOf(ticket.getTicketNumber()),
                                                    null,
                                                    "QUEUED",
                                                    LocalDateTime.now(),
                                                    ticket.getPickupTime()
                                            );
                                            
                                            try {
                                                OutboxEvent outboxPlaced = new OutboxEvent(
                                                        "ORDER",
                                                        savedOrder.getId(),
                                                        "cafe.order.placed",
                                                        io.r2dbc.postgresql.codec.Json.of(objectMapper.writeValueAsString(placedEvent))
                                                );
                                                
                                                OutboxEvent outboxStatus = new OutboxEvent(
                                                        "ORDER",
                                                        savedOrder.getId(),
                                                        "cafe.order.status.changed",
                                                        io.r2dbc.postgresql.codec.Json.of(objectMapper.writeValueAsString(statusEvent))
                                                );
                                                
                                                // Simulasikan error untuk test atomicity jika ada trigger tertentu
                                                if ("FORCE_ERROR_ATOMICITY".equals(request.customerName())) {
                                                    return Mono.error(new RuntimeException("Forced error for atomicity test"));
                                                }
                                                
                                                return outboxEventRepository.save(outboxPlaced)
                                                        .then(outboxEventRepository.save(outboxStatus))
                                                        .thenReturn(new OrderResult(savedOrder, items, ticket, List.of(savedHistory), placedEvent, statusEvent));
                                            } catch (JsonProcessingException e) {
                                                return Mono.error(new RuntimeException("Failed to serialize events", e));
                                            }
                                        });
                            });
                })
                .as(transactionalOperator::transactional) // Atomicity constraint
                .doOnSuccess(result -> {
                    // Fast-path publish
                    orderEventProducer.publishOrderPlaced(result.placedEvent())
                            .onErrorResume(e -> Mono.empty())
                            .subscribe();
                    orderEventProducer.publishOrderStatusChanged(result.statusEvent())
                            .onErrorResume(e -> Mono.empty())
                            .subscribe();
                })
                .map(result -> mapToOrderResponse(result.order(), result.items(), result.ticket(), result.histories()));
    }

    private record OrderResult(Order order, List<OrderItem> items, QueueTicket ticket, List<QueueTicketHistory> histories, OrderPlacedEvent placedEvent, OrderStatusChangedEvent statusEvent) {}

    public Mono<OrderResponse> getOrder(UUID orderId) {
        Mono<Order> orderMono = orderRepository.findByIdAndNotDeleted(orderId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")));
        
        Mono<List<OrderItem>> itemsMono = orderItemRepository.findByOrderIdAndNotDeleted(orderId).collectList();
        
        Mono<QueueTicket> ticketMono = queueTicketRepository.findByOrderIdAndNotDeleted(orderId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found")));

        return Mono.zip(orderMono, itemsMono, ticketMono)
                .flatMap(tuple -> {
                    Order order = tuple.getT1();
                    List<OrderItem> items = tuple.getT2();
                    QueueTicket ticket = tuple.getT3();

                    return queueTicketHistoryRepository.findByQueueTicketIdOrderByStatusTimestampAsc(ticket.getId())
                            .collectList()
                            .map(histories -> mapToOrderResponse(order, items, ticket, histories));
                });
    }

    public Mono<Void> cancelOrder(UUID orderId) {
        return updateOrderStatus(orderId, "CANCELLED", "QUEUED");
    }

    public Mono<Void> updateOrderStatus(UUID orderId, String newStatus) {
        return updateOrderStatus(orderId, newStatus, null);
    }
    
    private Mono<Void> updateOrderStatus(UUID orderId, String newStatus, String expectedPreviousStatus) {
        return orderRepository.findByIdAndNotDeleted(orderId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")))
                .flatMap(order -> {
                    if (expectedPreviousStatus != null && !expectedPreviousStatus.equals(order.getStatus())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Order status mismatch"));
                    }
                    
                    String previousStatus = order.getStatus();
                    order.setStatus(newStatus);
                    
                    return orderRepository.save(order)
                            .then(queueTicketRepository.findByOrderIdAndNotDeleted(orderId))
                            .flatMap(ticket -> {
                                ticket.setStatus(newStatus);
                                ticket.setStatusTimestamp(LocalDateTime.now());
                                return queueTicketRepository.save(ticket);
                            })
                            .flatMap(savedTicket -> {
                                QueueTicketHistory history = new QueueTicketHistory();
                                history.setQueueTicketId(savedTicket.getId());
                                history.setStatus(newStatus);
                                history.setStatusTimestamp(savedTicket.getStatusTimestamp());
                                history.setCreatedDate(LocalDateTime.now());
                                
                                return queueTicketHistoryRepository.save(history)
                                        .flatMap(savedHistory -> {
                                            OrderStatusChangedEvent statusEvent = new OrderStatusChangedEvent(
                                                    orderId,
                                                    String.valueOf(savedTicket.getTicketNumber()),
                                                    previousStatus,
                                                    newStatus,
                                                    LocalDateTime.now(),
                                                    savedTicket.getPickupTime()
                                            );
                                            try {
                                                OutboxEvent outboxStatus = new OutboxEvent(
                                                        "ORDER",
                                                        orderId,
                                                        "cafe.order.status.changed",
                                                        io.r2dbc.postgresql.codec.Json.of(objectMapper.writeValueAsString(statusEvent))
                                                );
                                                return outboxEventRepository.save(outboxStatus)
                                                        .thenReturn(statusEvent);
                                            } catch (JsonProcessingException e) {
                                                return Mono.error(new RuntimeException("Failed to serialize status event", e));
                                            }
                                        });
                            });
                })
                .as(transactionalOperator::transactional)
                .doOnSuccess(statusEvent -> {
                    // Fast-path publish
                    orderEventProducer.publishOrderStatusChanged(statusEvent)
                            .onErrorResume(e -> Mono.empty())
                            .subscribe();
                })
                .then();
    }

    private OrderResponse mapToOrderResponse(Order order, List<OrderItem> items, QueueTicket ticket, List<QueueTicketHistory> histories) {
        List<OrderResponse.OrderItemResponse> itemResponses = items.stream()
                .map(item -> new OrderResponse.OrderItemResponse(item.getMenuItemName(), item.getQuantity(), item.getPrice()))
                .toList();

        List<OrderResponse.QueueTicketHistoryResponse> historyResponses = histories.stream()
                .map(h -> new OrderResponse.QueueTicketHistoryResponse(h.getStatus(), h.getStatusTimestamp()))
                .toList();

        return new OrderResponse(
                order.getId(),
                ticket.getTicketNumber(),
                order.getCustomerName(),
                order.getOrderType(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedDate(),
                itemResponses,
                historyResponses
        );
    }
}
