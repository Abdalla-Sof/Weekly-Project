package com.digitalbank.accounts.Controlle.Service;

import com.digitalbank.accounts.dto.AccountResponse;
import com.digitalbank.accounts.dto.CreateAccountRequest;
import com.digitalbank.accounts.entity.Account;
import com.digitalbank.accounts.entity.AccountType;
import com.digitalbank.accounts.entity.Customer;
import com.digitalbank.accounts.exception.ResourceNotFoundException;
import com.digitalbank.accounts.repository.AccountRepository;
import com.digitalbank.accounts.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    @DisplayName("should throw when customer does not exist")
    void createAccount_whenCustomerDoesNotExist_throwsException() {
        UUID customerId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest(customerId, AccountType.SAVINGS);

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.createAccount(request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("should save a new savings account and return mapped response")
    void createAccount_whenCustomerExists_createsSavingsAccount() {
        UUID customerId = UUID.randomUUID();
        Customer customer = customer("Marco Bianchi", "marco@example.com", customerId);
        CreateAccountRequest request = new CreateAccountRequest(customerId, AccountType.SAVINGS);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.createAccount(request);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());

        Account savedAccount = captor.getValue();
        assertEquals(customerId, savedAccount.getCustomer().getId());
        assertEquals(AccountType.SAVINGS, savedAccount.getAccountType());
        assertNotNull(savedAccount.getAccountNumber());
        assertEquals(AccountType.SAVINGS, response.accountType());
        assertEquals(customerId, response.customerId());
    }

    @Test
    @DisplayName("should throw when account does not exist")
    void getAccount_whenAccountDoesNotExist_throwsException() {
        UUID accountId = UUID.randomUUID();

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.getAccount(accountId)
        );

        assertEquals("Account not found: " + accountId, exception.getMessage());
    }

    @Test
    @DisplayName("should map an existing account to response DTO")
    void getAccount_whenAccountExists_returnsAccountResponse() {
        UUID customerId = UUID.randomUUID();
        Customer customer = customer("Marco Bianchi", "marco@example.com", customerId);
        UUID accountId = UUID.randomUUID();
        Account account = account("ACC-123456789", customer, AccountType.SAVINGS, accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(accountId);

        assertEquals(accountId, response.id());
        assertEquals("ACC-123456789", response.accountNumber());
        assertEquals(AccountType.SAVINGS, response.accountType());
        assertEquals(customerId, response.customerId());
    }

    @Test
    @DisplayName("should return empty list when customer has no accounts")
    void getAccountsByCustomer_whenCustomerHasNoAccounts_returnsEmptyList() {
        UUID customerId = UUID.randomUUID();

        when(accountRepository.findByCustomer_Id(customerId)).thenReturn(List.of());

        List<AccountResponse> responses = accountService.getAccountsByCustomer(customerId);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(accountRepository).findByCustomer_Id(customerId);
    }

    @Test
    @DisplayName("should return both mapped accounts for a customer")
    void getAccountsByCustomer_whenCustomerHasTwoAccounts_returnsBoth() {
        UUID customerId = UUID.randomUUID();
        Customer customer = customer("Marco Bianchi", "marco@example.com", customerId);

        Account savingsAccount = account("ACC-100001", customer, AccountType.SAVINGS, UUID.randomUUID());
        Account currentAccount = account("ACC-100002", customer, AccountType.CURRENT, UUID.randomUUID());

        when(accountRepository.findByCustomer_Id(customerId)).thenReturn(List.of(savingsAccount, currentAccount));

        List<AccountResponse> responses = accountService.getAccountsByCustomer(customerId);

        assertEquals(2, responses.size());
        assertEquals("ACC-100001", responses.get(0).accountNumber());
        assertEquals(AccountType.SAVINGS, responses.get(0).accountType());
        assertEquals(customerId, responses.get(0).customerId());
        assertEquals("ACC-100002", responses.get(1).accountNumber());
        assertEquals(AccountType.CURRENT, responses.get(1).accountType());
        assertEquals(customerId, responses.get(1).customerId());
    }

    private Customer customer(String fullName, String email, UUID id) {
        Customer customer = new Customer(fullName, email);
        setField(customer, "id", id);
        return customer;
    }

    private Account account(String number, Customer customer, AccountType type, UUID id) {
        Account account = new Account(number, customer, type);
        setField(account, "id", id);
        return account;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Unable to set test field: " + fieldName, e);
        }
    }
}

