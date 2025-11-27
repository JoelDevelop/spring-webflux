package com.bankx.transactions_service.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTxRequest {

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String type; // CREDIT / DEBIT

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}