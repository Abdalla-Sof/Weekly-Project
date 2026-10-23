
package com.digitalbank.accounts.service;

import com.digitalbank.accounts.dto.CreateCustomerRequest;
import com.digitalbank.accounts.dto.CustomerResponse;
import com.digitalbank.accounts.entity.Customer;
import com.digitalbank.accounts.exception.DuplicateResourceException;
import com.digitalbank.accounts.exception.ResourceNotFoundException;
import com.digitalbank.accounts.repository.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    // TEST 1: Get an existing customer
    @Test
    void getCustomer_whenCustomerExists_returnsCustomerResponse() {

        // Arrange
        Customer customer = new Customer(
                "Marco Bianchi",
                "marco@example.com"
        );

        UUID customerId = customer.getId();

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        // Act
        CustomerResponse response =
                customerService.getCustomer(customerId);

        // Assert
        assertEquals(customerId, response.id());
        assertEquals("Marco Bianchi", response.fullName());
        assertEquals("marco@example.com", response.email());
    }

    // TEST 2: Customer does not exist
    @Test
    void getCustomer_whenCustomerDoesNotExist_throwsException() {

        // Arrange
        UUID customerId = UUID.randomUUID();

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

        // Act and Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.getCustomer(customerId)
        );

        assertEquals(
                "Customer not found: " + customerId,
                exception.getMessage()
        );
    }

    // TEST 3: Reject duplicate email
    @Test
    void createCustomer_whenEmailAlreadyExists_throwsException() {

        // Arrange
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Marco Bianchi",
                "marco@example.com"
        );

        when(customerRepository.existsByEmail("marco@example.com"))
                .thenReturn(true);

        // Act and Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> customerService.createCustomer(request)
        );

        assertEquals(
                "A customer with this email already exists.",
                exception.getMessage()
        );

        // Ensure no customer was saved
        verify(customerRepository, never())
                .save(any(Customer.class));
    }

    // TEST 4: Create a customer successfully
    @Test
    void createCustomer_whenEmailIsNew_savesCustomerSuccessfully() {

        // Arrange
        CreateCustomerRequest request = new CreateCustomerRequest(
                "  Marco Bianchi  ",
                "MARCO@EXAMPLE.COM"
        );

        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CustomerResponse response =
                customerService.createCustomer(request);

        // Assert: capture the Customer passed to save()
        ArgumentCaptor<Customer> captor =
                ArgumentCaptor.forClass(Customer.class);

        verify(customerRepository).save(captor.capture());

        Customer savedCustomer = captor.getValue();

        // Check that the service normalized the data
        assertEquals("Marco Bianchi", savedCustomer.getFullName());
        assertEquals("marco@example.com", savedCustomer.getEmail());

        // Check the response
        assertEquals("Marco Bianchi", response.fullName());
        assertEquals("marco@example.com", response.email());

        // Verify that the duplicate-email check occurred
        verify(customerRepository)
                .existsByEmail("marco@example.com");
    }
}
