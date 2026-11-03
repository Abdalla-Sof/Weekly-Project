
package com.digitalbank.loans.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanRequest(

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotNull(message = "Loan amount is required")
        @DecimalMin(value = "0.01", message = "Loan amount must be positive")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount

) {
}
