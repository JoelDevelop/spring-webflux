package com.bankx.transactions_service.api.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleBiz(BusinessException ex) {
        return Mono.just(
                ResponseEntity.badRequest().body(
                        Map.of("error", ex.getMessage())
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGen(Exception ex) {
        return Mono.just(
                ResponseEntity.status(500).body(
                        Map.of("error", "internal_error")
                )
        );
    }
}