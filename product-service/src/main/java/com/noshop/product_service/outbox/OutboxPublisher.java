package com.noshop.product_service.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findTop100ByStatusOrderByCreatedAtAsc(
                                OutboxStatus.PENDING
                        );

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {

        try {
            kafkaTemplate.send(
                    getTopic(event.getEventType()),
                    String.valueOf(event.getAggregateId()),
                    event.getPayload()
            ).get();

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(java.time.LocalDateTime.now());

            outboxEventRepository.save(event);

            log.info(
                    "Published outbox event. id={}, type={}, aggregateId={}",
                    event.getId(),
                    event.getEventType(),
                    event.getAggregateId()
            );

        } catch (Exception e) {

            event.setStatus(OutboxStatus.FAILED);
            outboxEventRepository.save(event);

            log.error(
                    "Failed to publish outbox event. id={}, type={}, aggregateId={}",
                    event.getId(),
                    event.getEventType(),
                    event.getAggregateId(),
                    e
            );
        }
    }

    private String getTopic(String eventType) {

        return switch (eventType) {
            case "PRODUCT_CREATED" -> "product-created";
            default -> throw new IllegalArgumentException(
                    "Unsupported outbox event type: " + eventType
            );
        };
    }
}