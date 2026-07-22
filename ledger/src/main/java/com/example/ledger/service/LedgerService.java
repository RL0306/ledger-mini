package com.example.ledger.service;


import com.example.ledger.model.Ledger;
import com.example.ledger.model.LedgerTransaction;
import com.example.ledger.repository.LedgerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import com.example.ledger.model.TransactionType;


import java.math.BigDecimal;


@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    public LedgerService(
            LedgerRepository ledgerRepository
    ) {
        this.ledgerRepository = ledgerRepository;
    }

    public LedgerTransaction recordMovement(
            TransactionType type,
            BigDecimal amount
    ) {
        Ledger ledger = ledgerRepository.getLedger();

        return ledger.recordMovement(type, amount);
    }

    public BigDecimal getCurrentBalance() {
        return ledgerRepository
                .getLedger()
                .getBalance();
    }

    public List<LedgerTransaction> getTransactionHistory() {
        return ledgerRepository
                .getLedger()
                .getTransactions();
    }
}