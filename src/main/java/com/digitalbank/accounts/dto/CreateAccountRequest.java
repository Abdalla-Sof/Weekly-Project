
package com.digitalbank.accounts.dto;

import com.digitalbank.accounts.entity.AccountType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAccountRequest(
        @NotNull UUID customerId,
        @NotNull AccountType accountType
) {
}
