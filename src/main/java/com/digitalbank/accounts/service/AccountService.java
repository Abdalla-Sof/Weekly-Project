
package com.digitalbank.accounts.service;

import com.digitalbank.accounts.dto.AccountResponse;
import com.digitalbank.accounts.dto.CreateAccountRequest;
import com.digitalbank.accounts.entity.Account;
import com.digitalbank.accounts.entity.Customer;
import com.digitalbank.accounts.exception.ResourceNotFoundException;
import com.digitalbank.accounts.repository.AccountRepository;
import com.digitalbank.accounts.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public AccountResponse createAccount(
            CreateAccountRequest request) {

        Customer customer = customerRepository
                .findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + request.customerId()));

        String accountNumber = "DB" +
                UUID.randomUUID().toString()
                        .replace("-", "")
                        .substring(0, 24)
                        .toUpperCase();

        Account account = new Account(
                accountNumber,
                customer,
                request.accountType()
        );

        Account saved = accountRepository.save(account);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + id));

        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getCustomerAccounts(
            UUID customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found: " + customerId);
        }

        return accountRepository.findByCustomer_Id(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomer().getId(),
                account.getAccountType(),
                account.getStatus(),
                account.getBalance(),
                account.getCreatedAt()
        );
    }
}
