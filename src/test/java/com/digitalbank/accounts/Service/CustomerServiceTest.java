package com.digitalbank.accounts.Controlle.Service;

import com.digitalbank.accounts.dto.CreateCustomerRequest;
import com.digitalbank.accounts.dto.CustomerResponse;
import com.digitalbank.accounts.dto.UpdateCustomerRequest;
import com.digitalbank.accounts.entity.Customer;
import com.digitalbank.accounts.exception.DuplicateResourceException;
import com.digitalbank.accounts.exception.ResourceNotFoundException;
import com.digitalbank.accounts.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    @DisplayName("should return a customer response when customer exists")
    void getCustomer_whenCustomerExists_returnsCustomerResponse() {
        UUID customerId = UUID.randomUUID();
        Customer customer = customer("Marco Bianchi", "marco@example.com", customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomer(customerId);

        assertNotNull(response);
        assertEquals(customerId, response.id());
        assertEquals("Marco Bianchi", response.fullName());
        assertEquals("marco@example.com", response.email());
    }

    @Test
    @DisplayName("should throw when customer does not exist")
    void getCustomer_whenCustomerDoesNotExist_throwsException() {
        UUID customerId = UUID.randomUUID();

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.getCustomer(customerId)
        );

        assertEquals("Customer not found: " + customerId, exception.getMessage());
    }

    @Test
    @DisplayName("should reject duplicate email before saving")
    void createCustomer_whenEmailAlreadyExists_throwsException() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Marco Bianchi",
                "marco@example.com"
        );

        when(customerRepository.existsByEmail("marco@example.com")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> customerService.createCustomer(request)
        );

        assertEquals("A customer with this email already exists.", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("should normalize name and email and save a new customer")
    void createCustomer_whenEmailIsNew_savesCustomerSuccessfully() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "  Marco Bianchi  ",
                "MARCO@EXAMPLE.COM"
        );

        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.createCustomer(request);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());

        Customer savedCustomer = captor.getValue();
        assertEquals("Marco Bianchi", savedCustomer.getFullName());
        assertEquals("marco@example.com", savedCustomer.getEmail());
        assertEquals("Marco Bianchi", response.fullName());
        assertEquals("marco@example.com", response.email());
        verify(customerRepository).existsByEmail("marco@example.com");
    }

    @Test
    @DisplayName("should update an existing customer when the new email is unique")
    void updateCustomer_whenCustomerExistsAndEmailIsUnique_updatesCustomer() {
        UUID customerId = UUID.randomUUID();
        Customer customer = customer("Maria Rossi", "maria@example.com", customerId);
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                " Maria Updated ",
                "NEW@EXAMPLE.COM"
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.updateCustomer(customerId, request);

        assertEquals("Maria Updated", response.fullName());
        assertEquals("new@example.com", response.email());
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("should reject update when another customer already owns the email")
    void updateCustomer_whenEmailAlreadyExists_throwsDuplicateResourceException() {
        UUID customerId = UUID.randomUUID();
        Customer customer = customer("Maria Rossi", "maria@example.com", customerId);
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "Maria Rossi",
                "existing@example.com"
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> customerService.updateCustomer(customerId, request)
        );

        assertEquals("Email already exists: existing@example.com", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    private Customer customer(String fullName, String email, UUID id) {
        Customer customer = new Customer(fullName, email);
        setField(customer, "id", id);
        return customer;
    }

    private void setField(Customer customer, String fieldName, UUID value) {
        try {
            java.lang.reflect.Field field = Customer.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(customer, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Unable to set test id", e);
        }
    }
}
