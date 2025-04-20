package com.brokerage.orderapp;

import com.brokerage.orderapp.entity.Asset;
import com.brokerage.orderapp.entity.Order;
import com.brokerage.orderapp.entity.OrderSide;
import com.brokerage.orderapp.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EntityMappingTests {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void whenSaveAsset_thenFindById() {
        Asset asset = Asset.builder()
                .customerId(1L)
                .assetName("TEST_ASSET")
                .size(BigDecimal.valueOf(100))
                .usableSize(BigDecimal.valueOf(100))
                .build();
        Asset saved = entityManager.persistFlushFind(asset);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAssetName()).isEqualTo("TEST_ASSET");
    }

    @Test
    void whenSaveOrder_thenFindById() {
        Order order = Order.builder()
                .customerId(1L)
                .assetName("TEST_ORDER")
                .orderSide(OrderSide.BUY)
                .size(BigDecimal.valueOf(10))
                .price(BigDecimal.valueOf(50))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();
        Order saved = entityManager.persistFlushFind(order);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderSide()).isEqualTo(OrderSide.BUY);
    }
}