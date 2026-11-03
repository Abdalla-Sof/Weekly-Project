package com.digitalbank.loans.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanResponse(
        UUID id,
        UUID customerId,
        String loanNumber,
        BigDecimal amount,
        String status,
        Instant createdAt
) {
}
