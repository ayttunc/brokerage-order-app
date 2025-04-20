package com.brokerage.orderapp.service;

import com.brokerage.orderapp.entity.Order;
import com.brokerage.orderapp.entity.OrderSide;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    Order createOrder(Long customerId, String assetName, OrderSide orderSide, BigDecimal size, BigDecimal price);
    void cancelOrder(Long orderId);
    List<Order> listOrders(Long customerId);
    List<Order> listOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate);
}