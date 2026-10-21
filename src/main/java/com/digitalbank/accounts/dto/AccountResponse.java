
package com.digitalbank.accounts.dto;

import com.digitalbank.accounts.entity.AccountStatus;
import com.digitalbank.accounts.entity.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        UUID customerId,
        AccountType accountType,
        AccountStatus status,
        BigDecimal balance,
        Instant createdAt
) {
}
