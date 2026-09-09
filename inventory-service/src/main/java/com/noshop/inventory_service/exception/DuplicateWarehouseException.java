package com.noshop.inventory_service.exception;

public class DuplicateWarehouseException extends RuntimeException {

    public DuplicateWarehouseException(String message) {
        super(message);
    }
}