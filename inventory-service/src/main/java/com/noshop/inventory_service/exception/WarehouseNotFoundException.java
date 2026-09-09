package com.noshop.inventory_service.exception;

public class WarehouseNotFoundException extends RuntimeException {

    public WarehouseNotFoundException(String message) {
        super(message);
    }
}