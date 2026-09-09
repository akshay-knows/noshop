package com.noshop.product_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic productCreatedTopic() {
        return new NewTopic(
                "product-created",
                1,
                (short) 1
        );
    }
}