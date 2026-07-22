package com.example.ledger.repository;


import com.example.ledger.model.Ledger;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLedgerRepository
        implements LedgerRepository {

    /*
     * This application supports one ledger.
     *
     * The ledger and its transaction history remain in memory
     * until the application is stopped.
     */
    private final Ledger ledger = new Ledger();

    @Override
    public Ledger getLedger() {
        return ledger;
    }
}