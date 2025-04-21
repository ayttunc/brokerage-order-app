package com.brokerage.orderapp.service.impl;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.entity.Order;
import com.brokerage.orderapp.entity.OrderSide;
import com.brokerage.orderapp.entity.OrderStatus;
import com.brokerage.orderapp.repository.AssetRepository;
import com.brokerage.orderapp.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Asset tryAsset;
    private Asset stockAsset;

    @BeforeEach
    void setUp() {
        tryAsset = Asset.builder()
                .id(1L)
                .customerId(100L)
                .assetName("TRY")
                .size(new BigDecimal("100000"))
                .usableSize(new BigDecimal("100000"))
                .build();

        stockAsset = Asset.builder()
                .id(2L)
                .customerId(100L)
                .assetName("ABC")
                .size(new BigDecimal("1000"))
                .usableSize(new BigDecimal("1000"))
                .build();
    }

    @Test
    void createOrder_buy_success() {
        BigDecimal size = new BigDecimal("10");
        BigDecimal price = new BigDecimal("100");
        when(assetRepository.findByCustomerIdAndAssetName(100L, "TRY"))
                .thenReturn(List.of(tryAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(99L);
            return o;
        });
        Order created = orderService.createOrder(100L, "ABC", OrderSide.BUY, size, price);
        assertNotNull(created.getId());
        assertEquals(OrderStatus.PENDING, created.getStatus());
        assertEquals(new BigDecimal("99000"), tryAsset.getUsableSize());
        verify(assetRepository).save(tryAsset);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_buy_insufficientTry_throws() {
        tryAsset.setUsableSize(new BigDecimal("50"));
        BigDecimal size = new BigDecimal("10");
        BigDecimal price = new BigDecimal("100");
        when(assetRepository.findByCustomerIdAndAssetName(100L, "TRY"))
                .thenReturn(List.of(tryAsset));
        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(100L, "ABC", OrderSide.BUY, size, price));
        verify(assetRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_sell_success() {
        BigDecimal size = new BigDecimal("100");
        when(assetRepository.findByCustomerIdAndAssetName(100L, "TRY"))
                .thenReturn(List.of(tryAsset));
        when(assetRepository.findByCustomerIdAndAssetName(100L, "ABC"))
                .thenReturn(List.of(stockAsset));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(100L);
            return o;
        });
        Order created = orderService.createOrder(100L, "ABC", OrderSide.SELL, size, BigDecimal.ZERO);
        assertNotNull(created.getId());
        assertEquals(new BigDecimal("900"), stockAsset.getUsableSize());
        verify(assetRepository).save(stockAsset);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_sell_insufficientAsset_throws() {
        stockAsset.setUsableSize(new BigDecimal("50"));
        BigDecimal size = new BigDecimal("100");
        when(assetRepository.findByCustomerIdAndAssetName(100L, "TRY"))
                .thenReturn(List.of(tryAsset));
        when(assetRepository.findByCustomerIdAndAssetName(100L, "ABC"))
                .thenReturn(List.of(stockAsset));
        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(100L, "ABC", OrderSide.SELL, size, BigDecimal.ZERO));
        verify(assetRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_pending_success() {
        BigDecimal size = new BigDecimal("10");
        BigDecimal price = new BigDecimal("100");
        Order order = Order.builder()
                .id(1L)
                .customerId(100L)
                .assetName("ABC")
                .orderSide(OrderSide.BUY)
                .size(size)
                .price(price)
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(100L, "TRY"))
                .thenReturn(List.of(tryAsset));
        when(orderRepository.save(order)).thenReturn(order);
        orderService.cancelOrder(1L);
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertEquals(new BigDecimal("101000"), tryAsset.getUsableSize());
        verify(assetRepository).save(tryAsset);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_nonPending_throws() {
        Order order = Order.builder()
                .id(2L)
                .customerId(100L)
                .assetName("ABC")
                .orderSide(OrderSide.BUY)
                .size(BigDecimal.ONE)
                .price(BigDecimal.ONE)
                .status(OrderStatus.MATCHED)
                .createDate(LocalDateTime.now())
                .build();
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(2L));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void listOrders_returnsAllOrdersForCustomer() {
        List<Order> expected = List.of(
                Order.builder()
                        .id(1L)
                        .customerId(100L)
                        .assetName("ABC")
                        .orderSide(OrderSide.BUY)
                        .size(BigDecimal.ONE)
                        .price(BigDecimal.ONE)
                        .status(OrderStatus.PENDING)
                        .createDate(LocalDateTime.now())
                        .build(),
                Order.builder()
                        .id(2L)
                        .customerId(100L)
                        .assetName("XYZ")
                        .orderSide(OrderSide.SELL)
                        .size(BigDecimal.ONE)
                        .price(BigDecimal.ONE)
                        .status(OrderStatus.PENDING)
                        .createDate(LocalDateTime.now())
                        .build()
        );
        when(orderRepository.findByCustomerId(100L)).thenReturn(expected);
        List<Order> result = orderService.listOrders(100L);
        assertEquals(expected, result);
        verify(orderRepository).findByCustomerId(100L);
    }

    @Test
    void listOrders_betweenDates_returnsFilteredOrders() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<Order> expected = List.of(
                Order.builder()
                        .id(3L)
                        .customerId(100L)
                        .assetName("ABC")
                        .orderSide(OrderSide.BUY)
                        .size(BigDecimal.ONE)
                        .price(BigDecimal.ONE)
                        .status(OrderStatus.PENDING)
                        .createDate(start.plusHours(1))
                        .build()
        );
        when(orderRepository.findByCustomerIdAndCreateDateBetween(100L, start, end)).thenReturn(expected);
        List<Order> result = orderService.listOrders(100L, start, end);
        assertEquals(expected, result);
        verify(orderRepository).findByCustomerIdAndCreateDateBetween(100L, start, end);
    }
}