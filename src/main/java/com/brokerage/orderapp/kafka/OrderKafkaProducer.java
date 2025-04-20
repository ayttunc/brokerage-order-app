package com.brokerage.orderapp.kafka;

import com.brokerage.orderapp.kafka.dto.OrderMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private final KafkaTemplate<String, OrderMessage> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(OrderKafkaProducer.class);
    private static final String TOPIC = "order-topic";

    public void sendOrder(OrderMessage message) {
        log.info("Producing order message: {}", message);
        kafkaTemplate.send(TOPIC, message.getTraceId(), message);
    }
}
