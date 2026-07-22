package com.example.ledger.model;

import com.example.ledger.exception.InsufficientFundsException;
import com.example.ledger.exception.InvalidMovementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerTest {

    private Ledger ledger;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
    }

    @Test
    void newLedgerStartsWithZeroBalanceAndNoTransactions() {
        assertAll(
                () -> assertEquals(new BigDecimal("0.00"), ledger.getBalance()),
                () -> assertTrue(ledger.getTransactions().isEmpty())
        );
    }

    @Test
    void depositUpdatesBalanceAndRecordsTransaction() {
        LedgerTransaction transaction = ledger.recordMovement(
                TransactionType.DEPOSIT,
                new BigDecimal("25.50")
        );

        assertAll(
                () -> assertNotNull(transaction.id()),
                () -> assertEquals(TransactionType.DEPOSIT, transaction.type()),
                () -> assertEquals(new BigDecimal("25.50"), transaction.amount()),
                () -> assertEquals(new BigDecimal("25.50"), transaction.balanceAfter()),
                () -> assertNotNull(transaction.createdAt()),
                () -> assertEquals(new BigDecimal("25.50"), ledger.getBalance()),
                () -> assertEquals(List.of(transaction), ledger.getTransactions())
        );
    }

    @Test
    void amountWithOneDecimalPlaceIsNormalisedToTwoDecimalPlaces() {
        LedgerTransaction transaction = ledger.recordMovement(
                TransactionType.DEPOSIT,
                new BigDecimal("10.5")
        );

        assertAll(
                () -> assertEquals(new BigDecimal("10.50"), transaction.amount()),
                () -> assertEquals(new BigDecimal("10.50"), transaction.balanceAfter()),
                () -> assertEquals(new BigDecimal("10.50"), ledger.getBalance())
        );
    }

    @Test
    void withdrawalUpdatesBalanceAndRecordsTransaction() {
        ledger.recordMovement(
                TransactionType.DEPOSIT,
                new BigDecimal("100.00")
        );

        LedgerTransaction withdrawal = ledger.recordMovement(
                TransactionType.WITHDRAWAL,
                new BigDecimal("35.25")
        );

        assertAll(
                () -> assertEquals(new BigDecimal("64.75"), ledger.getBalance()),
                () -> assertEquals(new BigDecimal("64.75"), withdrawal.balanceAfter()),
                () -> assertEquals(2, ledger.getTransactions().size()),
                () -> assertEquals(withdrawal, ledger.getTransactions().get(1))
        );
    }

    @Test
    void withdrawingEntireBalanceIsAllowed() {
        ledger.recordMovement(
                TransactionType.DEPOSIT,
                new BigDecimal("40.00")
        );

        LedgerTransaction withdrawal = ledger.recordMovement(
                TransactionType.WITHDRAWAL,
                new BigDecimal("40.00")
        );

        assertAll(
                () -> assertEquals(new BigDecimal("0.00"), withdrawal.balanceAfter()),
                () -> assertEquals(new BigDecimal("0.00"), ledger.getBalance())
        );
    }

    @Test
    void withdrawalAboveBalanceIsRejectedWithoutChangingLedgerState() {
        LedgerTransaction deposit = ledger.recordMovement(
                TransactionType.DEPOSIT,
                new BigDecimal("20.00")
        );

        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> ledger.recordMovement(
                        TransactionType.WITHDRAWAL,
                        new BigDecimal("20.01")
                )
        );

        assertAll(
                () -> assertEquals(
                        "Withdrawal cannot exceed the current balance",
                        exception.getMessage()
                ),
                () -> assertEquals(new BigDecimal("20.00"), ledger.getBalance()),
                () -> assertEquals(List.of(deposit), ledger.getTransactions())
        );
    }

    @Test
    void nullTransactionTypeIsRejectedWithoutChangingLedgerState() {
        InvalidMovementException exception = assertThrows(
                InvalidMovementException.class,
                () -> ledger.recordMovement(
                        null,
                        new BigDecimal("10.00")
                )
        );

        assertLedgerStillEmpty(exception, "Transaction type is required");
    }

    @Test
    void nullAmountIsRejectedWithoutChangingLedgerState() {
        InvalidMovementException exception = assertThrows(
                InvalidMovementException.class,
                () -> ledger.recordMovement(
                        TransactionType.DEPOSIT,
                        null
                )
        );

        assertLedgerStillEmpty(exception, "Amount is required");
    }

    @Test
    void zeroAmountIsRejectedWithoutChangingLedgerState() {
        InvalidMovementException exception = assertThrows(
                InvalidMovementException.class,
                () -> ledger.recordMovement(
                        TransactionType.DEPOSIT,
                        BigDecimal.ZERO
                )
        );

        assertLedgerStillEmpty(exception, "Amount must be greater than zero");
    }

    @Test
    void negativeAmountIsRejectedWithoutChangingLedgerState() {
        InvalidMovementException exception = assertThrows(
                InvalidMovementException.class,
                () -> ledger.recordMovement(
                        TransactionType.DEPOSIT,
                        new BigDecimal("-0.01")
                )
        );

        assertLedgerStillEmpty(exception, "Amount must be greater than zero");
    }

    @Test
    void amountWithMoreThanTwoDecimalPlacesIsRejectedWithoutChangingLedgerState() {
        InvalidMovementException exception = assertThrows(
                InvalidMovementException.class,
                () -> ledger.recordMovement(
                        TransactionType.DEPOSIT,
                        new BigDecimal("10.001")
                )
        );

        assertLedgerStillEmpty(
                exception,
                "Amount cannot have more than two decimal places"
        );
    }

    @Test
    void returnedTransactionHistoryCannotBeModified() {
        ledger.recordMovement(
                TransactionType.DEPOSIT,
                new BigDecimal("10.00")
        );

        List<LedgerTransaction> history = ledger.getTransactions();

        assertThrows(
                UnsupportedOperationException.class,
                () -> history.clear()
        );
        assertFalse(ledger.getTransactions().isEmpty());
    }

    private void assertLedgerStillEmpty(
            InvalidMovementException exception,
            String expectedMessage
    ) {
        assertAll(
                () -> assertEquals(expectedMessage, exception.getMessage()),
                () -> assertEquals(new BigDecimal("0.00"), ledger.getBalance()),
                () -> assertTrue(ledger.getTransactions().isEmpty())
        );
    }
}
