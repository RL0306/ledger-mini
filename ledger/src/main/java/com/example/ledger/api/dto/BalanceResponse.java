package com.example.ledger.api.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal balance
) {
}