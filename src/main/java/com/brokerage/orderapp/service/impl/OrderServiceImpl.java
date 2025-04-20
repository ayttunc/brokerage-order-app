package com.brokerage.orderapp.service.impl;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.entity.Order;
import com.brokerage.orderapp.entity.OrderSide;
import com.brokerage.orderapp.entity.OrderStatus;
import com.brokerage.orderapp.repository.AssetRepository;
import com.brokerage.orderapp.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.brokerage.orderapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    @Override
    public Order createOrder(Long customerId, String assetName, OrderSide orderSide, BigDecimal size, BigDecimal price) {
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, "TRY").stream().findFirst().orElseThrow();
        if (orderSide == OrderSide.BUY) {
            BigDecimal totalCost = price.multiply(size);
            if (tryAsset.getUsableSize().compareTo(totalCost) < 0) {
                throw new IllegalArgumentException("Insufficient TRY balance");
            }
            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalCost));
            assetRepository.save(tryAsset);
        } else {
            Asset sellAsset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName).stream().findFirst().orElseThrow();
            if (sellAsset.getUsableSize().compareTo(size) < 0) {
                throw new IllegalArgumentException("Insufficient asset balance");
            }
            sellAsset.setUsableSize(sellAsset.getUsableSize().subtract(size));
            assetRepository.save(sellAsset);
        }
        Order order = Order.builder()
                .customerId(customerId)
                .assetName(assetName)
                .orderSide(orderSide)
                .size(size)
                .price(price)
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();
        return orderRepository.save(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be canceled");
        }
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
        BigDecimal size = order.getSize();
        BigDecimal price = order.getPrice();
        if (order.getOrderSide() == OrderSide.BUY) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), "TRY").stream().findFirst().orElseThrow();
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(price.multiply(size)));
            assetRepository.save(tryAsset);
        } else {
            Asset sellAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName()).stream().findFirst().orElseThrow();
            sellAsset.setUsableSize(sellAsset.getUsableSize().add(size));
            assetRepository.save(sellAsset);
        }
    }

    @Override
    public List<Order> listOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> listOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }
}