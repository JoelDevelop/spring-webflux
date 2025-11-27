package com.bankx.transactions_service.repository;

import com.bankx.transactions_service.domain.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

    Mono<Account> findByNumber(String number);
}