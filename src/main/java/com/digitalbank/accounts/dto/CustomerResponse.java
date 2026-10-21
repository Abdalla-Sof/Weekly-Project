
package com.digitalbank.accounts.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String fullName,
        String email,
        Instant createdAt
) {
}
