package com.example.ledger.model;

import com.example.ledger.exception.InsufficientFundsException;
import com.example.ledger.exception.InvalidMovementException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ledger {

    private BigDecimal balance = new BigDecimal("0.00");

    /*
     * This ArrayList is the in-memory data store for the
     * transaction history.
     */
    private final List<LedgerTransaction> transactions =
            new ArrayList<>();

    public synchronized LedgerTransaction recordMovement(
            TransactionType type,
            BigDecimal amount
    ) {
        BigDecimal normalisedAmount =
                validateAndNormalise(type, amount);

        BigDecimal updatedBalance =
                calculateUpdatedBalance(type, normalisedAmount);

        LedgerTransaction transaction =
                new LedgerTransaction(
                        UUID.randomUUID(),
                        type,
                        normalisedAmount,
                        updatedBalance,
                        Instant.now()
                );

        balance = updatedBalance;
        transactions.add(transaction);

        return transaction;
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }

    public synchronized List<LedgerTransaction> getTransactions() {
        return List.copyOf(transactions);
    }

    private BigDecimal calculateUpdatedBalance(
            TransactionType type,
            BigDecimal amount
    ) {
        return switch (type) {
            case DEPOSIT -> balance.add(amount);

            case WITHDRAWAL -> {
                if (amount.compareTo(balance) > 0) {
                    throw new InsufficientFundsException(
                            "Withdrawal cannot exceed the current balance"
                    );
                }

                yield balance.subtract(amount);
            }
        };
    }

    private BigDecimal validateAndNormalise(
            TransactionType type,
            BigDecimal amount
    ) {
        /*
         * Jakarta validates HTTP requests, but the domain
         * protects itself in case it is called elsewhere.
         */
        if (type == null) {
            throw new InvalidMovementException(
                    "Transaction type is required"
            );
        }

        if (amount == null) {
            throw new InvalidMovementException(
                    "Amount is required"
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidMovementException(
                    "Amount must be greater than zero"
            );
        }

        try {
            return amount.setScale(
                    2,
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new InvalidMovementException(
                    "Amount cannot have more than two decimal places"
            );
        }
    }
}