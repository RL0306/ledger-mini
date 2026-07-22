package com.example.ledger.exception;

public class InsufficientFundsException
        extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}