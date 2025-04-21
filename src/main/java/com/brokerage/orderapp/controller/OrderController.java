package com.brokerage.orderapp.controller;

import com.brokerage.orderapp.dto.OrderRequest;
import com.brokerage.orderapp.entity.Order;
import com.brokerage.orderapp.kafka.OrderKafkaProducer;
import com.brokerage.orderapp.kafka.dto.OrderMessage;
import com.brokerage.orderapp.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderKafkaProducer orderKafkaProducer;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #request.customerId.toString() == principal.username)")
    public ResponseEntity<String> createOrder(@Valid @RequestBody OrderRequest request) {

        OrderMessage message = OrderMessage.builder()
                .traceId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .customerId(request.getCustomerId())
                .assetName(request.getAssetName())
                .orderSide(request.getOrderSide())
                .size(request.getSize())
                .price(request.getPrice())
                .build();

        orderKafkaProducer.sendOrder(message);
        return ResponseEntity.ok("Order is being processed asynchronously.");

    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @orderService.findById(#orderId).customerId.toString() == principal.username)")
    public ResponseEntity<Void> cancelOrder(@PathVariable @NotNull(message = "Order ID is required") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId.toString() == principal.username)")
    public ResponseEntity<List<Order>> listOrders(
            @RequestParam @NotNull(message = "Customer ID is required") Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {
        List<Order> orders = (startDate != null && endDate != null)
                ? orderService.listOrders(customerId, startDate, endDate)
                : orderService.listOrders(customerId);
        return ResponseEntity.ok(orders);
    }
}