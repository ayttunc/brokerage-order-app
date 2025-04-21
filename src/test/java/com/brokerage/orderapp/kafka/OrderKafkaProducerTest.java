package com.brokerage.orderapp.kafka;

import com.brokerage.orderapp.kafka.dto.OrderMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, OrderMessage> kafkaTemplate;

    @InjectMocks
    private OrderKafkaProducer producer;

    @Test
    void sendOrder_sendsToCorrectTopic() {
        OrderMessage msg = OrderMessage.builder()
                .traceId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .customerId(100L)
                .assetName("ABC")
                .orderSide(com.brokerage.orderapp.entity.OrderSide.BUY)
                .size(BigDecimal.ONE)
                .price(BigDecimal.ONE)
                .build();
        producer.sendOrder(msg);

        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<OrderMessage> msgCap = ArgumentCaptor.forClass(OrderMessage.class);

        verify(kafkaTemplate).send(eq("order-topic"), keyCap.capture(), msgCap.capture());
        assertEquals(msg.getTraceId(), keyCap.getValue());
        assertEquals(msg, msgCap.getValue());
    }
}
