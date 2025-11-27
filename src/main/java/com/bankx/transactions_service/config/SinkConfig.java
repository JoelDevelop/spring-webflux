package com.bankx.transactions_service.config;

import com.bankx.transactions_service.domain.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class SinkConfig {

    @Bean
    public Sinks.Many<Transaction> txSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}
