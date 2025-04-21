package com.brokerage.orderapp.kafka;

import com.brokerage.orderapp.entity.OrderStatus;
import com.brokerage.orderapp.kafka.dto.OrderMessage;
import com.brokerage.orderapp.repository.AssetRepository;
import com.brokerage.orderapp.repository.OrderRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "jwt.secret=test-secret",
        "jwt.expiration=3600000"
})
@EmbeddedKafka(partitions = 1, topics = {"order-topic"})
class OrderKafkaFlowIT {

    @Autowired
    private KafkaTemplate<String, OrderMessage> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AssetRepository assetRepository;

    private long testCustomerId;

    @BeforeEach
    void clean() {
        orderRepository.deleteAll();
        assetRepository.deleteAll();

        testCustomerId = System.currentTimeMillis();

        assetRepository.save(new com.brokerage.orderapp.entity.Asset(
                null,
                testCustomerId,
                "TRY",
                BigDecimal.ZERO,
                new BigDecimal("1000")
        ));
        assetRepository.save(new com.brokerage.orderapp.entity.Asset(
                null,
                testCustomerId,
                "ABC",
                BigDecimal.ZERO,
                BigDecimal.ZERO
        ));
    }

    @Test
    void kafkaFlow_persistsOrder() {
        String trace = UUID.randomUUID().toString();
        OrderMessage msg = OrderMessage.builder()
                .traceId(trace)
                .createdAt(Instant.now())
                .customerId(testCustomerId)
                .assetName("ABC")
                .orderSide(com.brokerage.orderapp.entity.OrderSide.BUY)
                .size(BigDecimal.ONE)
                .price(BigDecimal.ONE)
                .build();
        kafkaTemplate.send("order-topic", trace, msg);
        kafkaTemplate.flush();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = orderRepository.count();
            System.out.println("Order count so far: " + count);
            assertEquals(1, count);
        });

        assertEquals(OrderStatus.PENDING, orderRepository.findAll().get(0).getStatus());
    }
}
