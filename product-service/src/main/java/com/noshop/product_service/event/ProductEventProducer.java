package com.noshop.product_service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private static final String PRODUCT_CREATED_TOPIC = "product-created";

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    public void publishProductCreated(ProductCreatedEvent event) {
        kafkaTemplate.send(
                PRODUCT_CREATED_TOPIC,
                String.valueOf(event.getProductId()),
                event
        );
    }
}