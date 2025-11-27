package com.bankx.transactions_service;

import com.bankx.transactions_service.service.RiskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@SpringBootTest
class RiskServiceTest {

    @Autowired
    RiskService riskService;

    @Test
    void allowsDebitUnderLimit() {
        StepVerifier.create(
                        riskService.isAllowed("PEN", "DEBIT", new BigDecimal("100"))
                )
                .expectNext(true)
                .verifyComplete();
    }
}