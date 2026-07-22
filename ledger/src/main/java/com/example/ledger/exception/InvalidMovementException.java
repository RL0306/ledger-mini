package com.example.ledger.exception;

public class InvalidMovementException
        extends RuntimeException {

    public InvalidMovementException(String message) {
        super(message);
    }
}