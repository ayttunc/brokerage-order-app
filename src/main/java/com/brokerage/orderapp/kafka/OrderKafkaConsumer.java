package com.brokerage.orderapp.kafka;

import com.brokerage.orderapp.kafka.dto.OrderMessage;
import com.brokerage.orderapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "order-topic", groupId = "order-consumer-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrder(OrderMessage message) {
        log.info("Consumed order message with traceId {}: {}", message.getTraceId(), message);
        try {
            orderService.createOrder(
                    message.getCustomerId(),
                    message.getAssetName(),
                    message.getOrderSide(),
                    message.getSize(),
                    message.getPrice()
            );
        } catch (Exception e) {
            log.error("Error processing order with traceId {}: {}", message.getTraceId(), e.getMessage());
        }
    }
}
