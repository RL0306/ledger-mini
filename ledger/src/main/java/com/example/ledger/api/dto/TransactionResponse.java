package com.example.ledger.api.dto;

import com.example.ledger.model.LedgerTransaction;
import com.example.ledger.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant createdAt
) {

    public static TransactionResponse from(
            LedgerTransaction transaction
    ) {
        return new TransactionResponse(
                transaction.id(),
                transaction.type(),
                transaction.amount(),
                transaction.balanceAfter(),
                transaction.createdAt()
        );
    }
}
