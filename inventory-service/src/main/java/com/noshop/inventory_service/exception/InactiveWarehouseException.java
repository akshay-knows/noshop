package com.noshop.inventory_service.exception;

public class InactiveWarehouseException extends RuntimeException {

    public InactiveWarehouseException(String message) {
        super(message);
    }
}