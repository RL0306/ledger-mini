package com.example.ledger.api;

import com.example.ledger.exception.InsufficientFundsException;
import com.example.ledger.model.LedgerTransaction;
import com.example.ledger.model.TransactionType;
import com.example.ledger.service.LedgerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LedgerController.class)
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LedgerService ledgerService;

    @Test
    void postTransactionReturnsCreatedTransaction() throws Exception {
        UUID id = UUID.fromString("79bb10cf-07f2-4d5e-a16a-652a5037f8da");
        Instant createdAt = Instant.parse("2026-07-22T10:15:30Z");
        LedgerTransaction transaction = new LedgerTransaction(
                id,
                TransactionType.DEPOSIT,
                new BigDecimal("25.50"),
                new BigDecimal("25.50"),
                createdAt
        );

        when(ledgerService.recordMovement(
                TransactionType.DEPOSIT,
                new BigDecimal("25.50")
        )).thenReturn(transaction);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "DEPOSIT",
                                  "amount": 25.50
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(25.50))
                .andExpect(jsonPath("$.balanceAfter").value(25.50))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

        verify(ledgerService).recordMovement(
                TransactionType.DEPOSIT,
                new BigDecimal("25.50")
        );
    }

    @Test
    void getBalanceReturnsCurrentBalance() throws Exception {
        when(ledgerService.getCurrentBalance())
                .thenReturn(new BigDecimal("75.25"));

        mockMvc.perform(get("/api/v1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(75.25));
    }

    @Test
    void getTransactionsReturnsMappedTransactionHistory() throws Exception {
        LedgerTransaction first = new LedgerTransaction(
                UUID.fromString("cb01ff1f-ef78-4538-b55a-4fc0ecb1ea0d"),
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                Instant.parse("2026-07-22T10:00:00Z")
        );
        LedgerTransaction second = new LedgerTransaction(
                UUID.fromString("46d58a98-9387-4eeb-a497-36a30b4dc452"),
                TransactionType.WITHDRAWAL,
                new BigDecimal("30.00"),
                new BigDecimal("70.00"),
                Instant.parse("2026-07-22T10:05:00Z")
        );

        when(ledgerService.getTransactionHistory())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].balanceAfter").value(100.00))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$[1].balanceAfter").value(70.00));
    }

    @Test
    void zeroAmountReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "DEPOSIT",
                                  "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Amount must be greater than zero"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void amountWithMoreThanTwoDecimalPlacesReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "DEPOSIT",
                                  "amount": 10.001
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Amount cannot contain more than two decimal places"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void missingTransactionTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Transaction type is required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unknownTransactionTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "TRANSFER",
                                  "amount": 10.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Request body is invalid"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void insufficientFundsReturnsBadRequest() throws Exception {
        when(ledgerService.recordMovement(
                TransactionType.WITHDRAWAL,
                new BigDecimal("50.00")
        )).thenThrow(new InsufficientFundsException(
                "Withdrawal cannot exceed the current balance"
        ));

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "WITHDRAWAL",
                                  "amount": 50.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Withdrawal cannot exceed the current balance"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
