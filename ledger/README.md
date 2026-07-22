# Ledger API

A simple Spring Boot REST API for recording deposits and withdrawals, checking the current balance, and viewing transaction history.

Data is stored in memory and resets when the application restarts.

## Requirements

- Java 21

The Maven Wrapper is included, so Maven does not need to be installed separately.

## Run the application

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS/Linux

```bash
./mvnw spring-boot:run
```

The application runs at:

```text
http://localhost:8080
```

## Run the tests

### Windows

```powershell
.\mvnw.cmd test
```

### macOS/Linux

```bash
./mvnw test
```

## API Examples

### Make a deposit

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"type":"DEPOSIT","amount":100.00}'
```

### Make a withdrawal

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"type":"WITHDRAWAL","amount":25.00}'
```

Withdrawals greater than the current balance will be rejected.

### Get the current balance

```bash
curl http://localhost:8080/api/v1/balance
```

Example response:

```json
{
  "balance": 75.00
}
```

### Get transaction history

```bash
curl http://localhost:8080/api/v1/transactions
```

## Validation

Transactions must:

- Have a type of `DEPOSIT` or `WITHDRAWAL`
- Have an amount greater than zero
- Have no more than two decimal places
