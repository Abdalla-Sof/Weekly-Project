
package com.digitalbank.accounts.controller;

import com.digitalbank.accounts.dto.AccountResponse;
import com.digitalbank.accounts.dto.CreateAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountService {

    private final com.digitalbank.accounts.service.AccountService accountService;

    public AccountService(com.digitalbank.accounts.service.AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable UUID id) {
        return accountService.getAccount(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<AccountResponse> getCustomerAccounts(
            @PathVariable UUID customerId) {
        return accountService.getCustomerAccounts(customerId);
    }
}
