package com.brokerage.orderapp.service;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.entity.Order;
import com.brokerage.orderapp.entity.OrderSide;
import com.brokerage.orderapp.entity.OrderStatus;
import com.brokerage.orderapp.repository.AssetRepository;
import com.brokerage.orderapp.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "jwt.secret=test-secret",
        "jwt.expiration=3600000",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
@Transactional
class OrderServiceImplIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void init() {
        orderRepository.deleteAll();
        assetRepository.deleteAll();

        assetRepository.save(
                Asset.builder()
                        .customerId(100L)
                        .assetName("TRY")
                        .size(new BigDecimal("100000"))
                        .usableSize(new BigDecimal("100000"))
                        .build()
        );
        assetRepository.save(
                Asset.builder()
                        .customerId(100L)
                        .assetName("ABC")
                        .size(BigDecimal.ZERO)
                        .usableSize(BigDecimal.ZERO)
                        .build()
        );
    }

    @Test
    void createOrderBuy_persistsAndUpdatesTry() {
        Order order = orderService.createOrder(
                100L, "ABC", OrderSide.BUY,
                new BigDecimal("10"), new BigDecimal("100")
        );

        assertNotNull(order.getId());
        assertEquals(OrderStatus.PENDING, order.getStatus());

        Asset tryAsset = assetRepository
                .findByCustomerIdAndAssetName(100L, "TRY").get(0);
        assertEquals(new BigDecimal("99000"), tryAsset.getUsableSize());

        Order dbOrder = orderRepository.findById(order.getId())
                .orElseThrow();
        assertEquals(order, dbOrder);
    }

    @Test
    void cancelOrderPending_updatesAssets() {
        Order order = orderService.createOrder(
                100L, "ABC", OrderSide.BUY,
                BigDecimal.ONE, BigDecimal.ONE
        );

        orderService.cancelOrder(order.getId());

        Order dbOrder = orderRepository.findById(order.getId())
                .orElseThrow();
        assertEquals(OrderStatus.CANCELED, dbOrder.getStatus());

        Asset tryAsset = assetRepository
                .findByCustomerIdAndAssetName(100L, "TRY").get(0);
        assertEquals(new BigDecimal("100000"), tryAsset.getUsableSize());
    }

    @Test
    void listOrders_betweenDates_returnsOnlyWithinRange() {
        LocalDateTime now = LocalDateTime.now();

        Order older = orderService.createOrder(
                100L, "ABC", OrderSide.BUY,
                BigDecimal.ONE, BigDecimal.ONE
        );
        older.setCreateDate(now.minusHours(3));
        orderRepository.save(older);

        Order inside = orderService.createOrder(
                100L, "ABC", OrderSide.BUY,
                BigDecimal.ONE, BigDecimal.ONE
        );
        inside.setCreateDate(now.minusHours(1));
        orderRepository.save(inside);

        LocalDateTime start = now.minusHours(2);
        LocalDateTime end = now;

        List<Order> result = orderService.listOrders(100L, start, end);

        assertEquals(List.of(inside), result);
    }
}
