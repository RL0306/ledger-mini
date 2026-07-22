package com.example.ledger.api;
import com.example.ledger.api.dto.BalanceResponse;
import com.example.ledger.api.dto.CreateTransactionRequest;
import com.example.ledger.api.dto.TransactionResponse;
import com.example.ledger.model.LedgerTransaction;
import com.example.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(
            LedgerService ledgerService
    ) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse recordMovement(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        LedgerTransaction transaction =
                ledgerService.recordMovement(
                        request.type(),
                        request.amount()
                );

        return TransactionResponse.from(transaction);
    }

    @GetMapping("/balance")
    public BalanceResponse getBalance() {
        return new BalanceResponse(
                ledgerService.getCurrentBalance()
        );
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> getTransactions() {
        return ledgerService
                .getTransactionHistory()
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
