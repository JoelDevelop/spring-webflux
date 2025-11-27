package com.bankx.transactions_service.service;

import com.bankx.transactions_service.api.dto.CreateTxRequest;
import com.bankx.transactions_service.api.error.BusinessException;
import com.bankx.transactions_service.domain.Account;
import com.bankx.transactions_service.domain.Transaction;
import com.bankx.transactions_service.repository.AccountRepository;
import com.bankx.transactions_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import org.springframework.http.codec.ServerSentEvent;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final RiskService riskService;
    private final Sinks.Many<Transaction> txSink;

    public Mono<Transaction> create(CreateTxRequest req) {
        return accountRepo.findByNumber(req.getAccountNumber())
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMap(acc -> validateAndApply(acc, req))
                .onErrorMap(IllegalStateException.class, e -> new BusinessException(e.getMessage()));
    }

    private Mono<Transaction> validateAndApply(Account acc, CreateTxRequest req) {
        String type = req.getType().toUpperCase();
        BigDecimal amount = req.getAmount();

        // 1) Riesgo
        return riskService.isAllowed(acc.getCurrency(), type, amount)
                .flatMap(allowed -> {
                    if (!allowed) {
                        return Mono.error(new BusinessException("risk_rejected"));
                    }

                    // 2) Regla de negocio: saldo suficiente
                    if ("DEBIT".equals(type) && acc.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new BusinessException("insufficient_funds"));
                    }

                    // 3) Actualizar balance de forma reactiva
                    return Mono.just(acc)
                            .publishOn(Schedulers.parallel())
                            .map(a -> {
                                BigDecimal newBal = "DEBIT".equals(type)
                                        ? a.getBalance().subtract(amount)
                                        : a.getBalance().add(amount);
                                a.setBalance(newBal);
                                return a;
                            })
                            .flatMap(accountRepo::save)
                            // 4) Persistir transacción
                            .flatMap(saved -> txRepo.save(
                                            Transaction.builder()
                                                    .accountId(saved.getId())
                                                    .type(type)
                                                    .amount(amount)
                                                    .timestamp(Instant.now())
                                                    .status("OK")
                                                    .build()
                                    )
                            )
                            // 5) Notificar por SSE
                            .doOnNext(tx -> txSink.tryEmitNext(tx));
                });
    }

    public Flux<Transaction> byAccount(String accountNumber) {
        return accountRepo.findByNumber(accountNumber)
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMapMany(acc ->
                        txRepo.findByAccountIdOrderByTimestampDesc(acc.getId())
                );
    }

    public Flux<ServerSentEvent<Transaction>> stream() {
        return txSink.asFlux()
                .map(tx -> ServerSentEvent.builder(tx)
                        .event("transaction")
                        .build());
    }
}
