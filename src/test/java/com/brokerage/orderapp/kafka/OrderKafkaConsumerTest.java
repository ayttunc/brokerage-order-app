package com.brokerage.orderapp.kafka;

import com.brokerage.orderapp.entity.OrderSide;
import com.brokerage.orderapp.kafka.dto.OrderMessage;
import com.brokerage.orderapp.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderKafkaConsumerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderKafkaConsumer consumer;

    @Test
    void consumeOrder_callsOrderService() {
        OrderMessage msg = OrderMessage.builder()
                .traceId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .customerId(100L)
                .assetName("ABC")
                .orderSide(OrderSide.BUY)
                .size(BigDecimal.ONE)
                .price(BigDecimal.ONE)
                .build();
        consumer.consumeOrder(msg);
        verify(orderService).createOrder(100L, "ABC", OrderSide.BUY, BigDecimal.ONE, BigDecimal.ONE);
    }
}
