package com.noshop.product_service.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}