
package com.dis.service;

import com.digitalbank.accounts.dto.AccountResponse;
import com.digitalbank.accounts.dto.CreateAccountRequest;

import com.digitalbank.accounts.entity.Account;
import com.digitalbank.accounts.entity.AccountType;
import com.digitalbank.accounts.entity.Customer;

import com.digitalbank.accounts.exception.ResourceNotFoundException;

import com.digitalbank.accounts.repository.AccountRepository;
import com.digitalbank.accounts.repository.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sun.java2d.cmm.ProfileDataVerifier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;


    // TEST 1: Reject account creation when customer does not exist
    @Test
    void createAccount_whenCustomerDoesNotExist_throwsException() {

        // ARRANGE
        UUID customerId = UUID.randomUUID();

        CreateAccountRequest request = new CreateAccountRequest(
                customerId,
                AccountType.SAVINGS
        );

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

        // ACT AND ASSERT
        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.createAccount(request)
        );

        // Verify no account was saved
        ProfileDataVerifier.verify(accountRepository, never())
                .save(any(Account.class));
    }


    // TEST 2: Create a savings account successfully
    @Test
    void createAccount_whenCustomerExists_createsSavingsAccount() {

        // ARRANGE
        Customer customer = new Customer(
                "Marco Bianchi",
                "marco@example.com"
        );

        UUID customerId = customer.getId();

        CreateAccountRequest request = new CreateAccountRequest(
                customerId,
                AccountType.SAVINGS
        );

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        AccountResponse response =
                accountService.createAccount(request);

        // ASSERT
        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        ProfileDataVerifier.verify(accountRepository).save(captor.capture());

        Account savedAccount = captor.getValue();

        // Verify account ownership
        assertEquals(
                customerId,
                savedAccount.getCustomer().getId()
        );

        // Verify account type
        assertEquals(
                AccountType.SAVINGS,
                savedAccount.getAccountType()
        );

        // Verify account number was generated
        assertNotNull(savedAccount.getAccountNumber());

        // Verify response
        assertEquals(
                AccountType.SAVINGS,
                response.accountType()
        );
    }


    // TEST 3: Reject retrieval when account does not exist
    @Test
    void getAccount_whenAccountDoesNotExist_throwsException() {

        // ARRANGE
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        // ACT AND ASSERT
        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.getAccount(accountId)
        );

        // Verify repository lookup
        ProfileDataVerifier.verify(accountRepository).findById(accountId);
    }


    // TEST 4: Retrieve an existing bank account
    @Test
    void getAccount_whenAccountExists_returnsAccountResponse() {

        // ARRANGE
        Customer customer = new Customer(
                "Marco Bianchi",
                "marco@example.com"
        );

        Account account = new Account(
                "ACC-123456789",
                customer,
                AccountType.SAVINGS
        );

        UUID accountId = account.getId();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        // ACT
        AccountResponse response =
                accountService.getAccount(accountId);

        // ASSERT
        assertEquals(accountId, response.id());

        assertEquals(
                "ACC-123456789",
                response.accountNumber()
        );

        assertEquals(
                AccountType.SAVINGS,
                response.accountType()
        );

        assertEquals(
                customer.getId(),
                response.customerId()
        );

        // Verify repository lookup
        ProfileDataVerifier.verify(accountRepository).findById(accountId);
    }


    // TEST 5: Return an empty list when customer has no accounts
    @Test
    void getAccountsByCustomer_whenCustomerHasNoAccounts_returnsEmptyList() {

        // ARRANGE
        UUID customerId = UUID.randomUUID();

        when(accountRepository.findByCustomer_Id(customerId))
                .thenReturn(List.of());

        // ACT
        List<AccountResponse> responses =
                accountService.getAccountsByCustomer(customerId);

        // ASSERT
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        // Verify repository lookup
        ProfileDataVerifier.verify(accountRepository)
                .findByCustomer_Id(customerId);
    }


    // TEST 6: Retrieve two accounts belonging to one customer
    @Test
    void getAccountsByCustomer_whenCustomerHasTwoAccounts_returnsBoth() {

        // ARRANGE
        Customer customer = new Customer(
                "Marco Bianchi",
                "marco@example.com"
        );

        UUID customerId = customer.getId();

        Account savingsAccount = new Account(
                "ACC-100001",
                customer,
                AccountType.SAVINGS
        );

        Account currentAccount = new Account(
                "ACC-100002",
                customer,
                AccountType.CURRENT
        );

        when(accountRepository.findByCustomer_Id(customerId))
                .thenReturn(List.of(
                        savingsAccount,
                        currentAccount
                ));

        // ACT
        List<AccountResponse> responses =
                accountService.getAccountsByCustomer(customerId);

        // ASSERT
        assertEquals(2, responses.size());

        // Verify savings account
        assertEquals(
                "ACC-100001",
                responses.get(0).accountNumber()
        );

        assertEquals(
                AccountType.SAVINGS,
                responses.get(0).accountType()
        );

        assertEquals(
                customerId,
                responses.get(0).customerId()
        );

        // Verify current account
        assertEquals(
                "ACC-100002",
                responses.get(1).accountNumber()
        );

        assertEquals(
                AccountType.CURRENT,
                responses.get(1).accountType()
        );

        assertEquals(
                customerId,
                responses.get(1).customerId()
        );

        // Verify repository lookup
        ProfileDataVerifier.verify(accountRepository)
                .findByCustomer_Id(customerId);
    }
}
