package com.brokerage.orderapp.service.impl;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.entity.Order;
import com.brokerage.orderapp.entity.OrderSide;
import com.brokerage.orderapp.entity.OrderStatus;
import com.brokerage.orderapp.repository.AssetRepository;
import com.brokerage.orderapp.repository.OrderRepository;
import com.brokerage.orderapp.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AssetRepository assetRepository;
    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_buy_sufficientFunds() {
        Long customerId = 1L;
        String assetName = "ABC";
        BigDecimal size = BigDecimal.valueOf(10);
        BigDecimal price = BigDecimal.valueOf(5);
        Asset tryAsset = Asset.builder()
                .id(100L)
                .customerId(customerId)
                .assetName("TRY")
                .size(BigDecimal.valueOf(100))
                .usableSize(BigDecimal.valueOf(100))
                .build();
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY"))
                .thenReturn(List.of(tryAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order order = orderService.createOrder(customerId, assetName, OrderSide.BUY, size, price);

        BigDecimal expectedRemaining = BigDecimal.valueOf(100).subtract(price.multiply(size));
        assertThat(tryAsset.getUsableSize()).isEqualByComparingTo(expectedRemaining);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(assetRepository).save(tryAsset);
        verify(orderRepository).save(order);
    }

    @Test
    void createOrder_buy_insufficientFunds() {
        Long customerId = 2L;
        BigDecimal size = BigDecimal.valueOf(10);
        BigDecimal price = BigDecimal.valueOf(20);
        Asset tryAsset = Asset.builder()
                .id(101L)
                .customerId(customerId)
                .assetName("TRY")
                .size(BigDecimal.valueOf(100))
                .usableSize(BigDecimal.valueOf(50))
                .build();
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY"))
                .thenReturn(List.of(tryAsset));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(customerId, "ABC", OrderSide.BUY, size, price));
        verify(assetRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_sell_sufficientAsset() {
        Long customerId = 3L;
        String assetName = "XYZ";
        BigDecimal size = BigDecimal.valueOf(15);
        BigDecimal price = BigDecimal.valueOf(2);
        Asset sellAsset = Asset.builder()
                .id(102L)
                .customerId(customerId)
                .assetName(assetName)
                .size(BigDecimal.valueOf(50))
                .usableSize(BigDecimal.valueOf(50))
                .build();
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName))
                .thenReturn(List.of(sellAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order order = orderService.createOrder(customerId, assetName, OrderSide.SELL, size, price);

        BigDecimal expectedRemaining = BigDecimal.valueOf(50).subtract(size);
        assertThat(sellAsset.getUsableSize()).isEqualByComparingTo(expectedRemaining);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(assetRepository).save(sellAsset);
        verify(orderRepository).save(order);
    }

    @Test
    void createOrder_sell_insufficientAsset() {
        Long customerId = 4L;
        String assetName = "XYZ";
        BigDecimal size = BigDecimal.valueOf(60);
        BigDecimal price = BigDecimal.valueOf(2);
        Asset sellAsset = Asset.builder()
                .id(103L)
                .customerId(customerId)
                .assetName(assetName)
                .size(BigDecimal.valueOf(50))
                .usableSize(BigDecimal.valueOf(30))
                .build();
        when(assetRepository.findByCustomerIdAndAssetName(customerId, assetName))
                .thenReturn(List.of(sellAsset));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(customerId, assetName, OrderSide.SELL, size, price));
        verify(assetRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_pendingBuy() {
        Long orderId = 5L;
        Long customerId = 6L;
        BigDecimal size = BigDecimal.valueOf(8);
        BigDecimal price = BigDecimal.valueOf(3);
        Order order = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .assetName("ABC")
                .orderSide(OrderSide.BUY)
                .size(size)
                .price(price)
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        Asset tryAsset = Asset.builder()
                .id(104L)
                .customerId(customerId)
                .assetName("TRY")
                .size(BigDecimal.valueOf(100))
                .usableSize(BigDecimal.valueOf(10))
                .build();
        when(assetRepository.findByCustomerIdAndAssetName(customerId, "TRY"))
                .thenReturn(List.of(tryAsset));

        orderService.cancelOrder(orderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        BigDecimal expectedRestore = BigDecimal.valueOf(10).add(price.multiply(size));
        assertThat(tryAsset.getUsableSize()).isEqualByComparingTo(expectedRestore);
        verify(assetRepository).save(tryAsset);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_nonPending() {
        Long orderId = 7L;
        Order order = Order.builder()
                .id(orderId)
                .customerId(8L)
                .assetName("ABC")
                .orderSide(OrderSide.SELL)
                .size(BigDecimal.ONE)
                .price(BigDecimal.ONE)
                .status(OrderStatus.CANCELED)
                .createDate(LocalDateTime.now())
                .build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class,
                () -> orderService.cancelOrder(orderId));
        verify(assetRepository, never()).save(any());
    }
}