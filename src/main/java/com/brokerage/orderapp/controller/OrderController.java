package com.brokerage.orderapp.controller;

import com.brokerage.orderapp.dto.OrderRequest;
import com.brokerage.orderapp.entity.Order;
import com.brokerage.orderapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        Order order = orderService.createOrder(
                request.getCustomerId(),
                request.getAssetName(),
                request.getOrderSide(),
                request.getSize(),
                request.getPrice()
        );
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Order>> listOrders(@RequestParam Long customerId,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Order> orders = (startDate != null && endDate != null)
                ? orderService.listOrders(customerId, startDate, endDate)
                : orderService.listOrders(customerId);
        return ResponseEntity.ok(orders);
    }
}