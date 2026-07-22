package com.example.ledger.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerTransaction(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant createdAt
) {
}