package com.example.ledger.api.dto;

import java.time.Instant;

public record ApiError(
        String message,
        Instant timestamp
) {
}