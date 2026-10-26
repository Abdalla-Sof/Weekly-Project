package com.digitalbank.accounts;
import com.digitalbank.accounts.dto.CreateCustomerRequest;
import com.digitalbank.accounts.dto.CustomerResponse;
import com.digitalbank.accounts.dto.UpdateCustomerRequest;
import com.digitalbank.accounts.exception.DuplicateResourceException;
import com.digitalbank.accounts.exception.ResourceNotFoundException;
import com.digitalbank.accounts.repository.CustomerRepository;
import com.digitalbank.accounts.service.CustomerService;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerIntegrationTest {

    @Test
    void applicationContextLoads() {

    }
    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @Transactional
    void createCustomer_shouldSaveCustomerInDatabase()  {

        // ARRANGE: Generate a unique email
        String email = "marco-" + UUID.randomUUID() + "@example.com";

        CreateCustomerRequest request = new CreateCustomerRequest(
                "Marco Bianchi",
                email
        );

        // ACT: Call the real service
        CustomerResponse response = customerService.createCustomer(request);

        // ASSERT: Verify the response
        assertNotNull(response.id());


        assertEquals("Marco Bianchi", response.fullName());

        assertEquals(email, response.email());

        // ASSERT: Verify the customer exists in H2
        assertTrue(customerRepository.existsById(response.id()));
        // End the test transaction and roll back its changes
        TestTransaction.flagForRollback();
        TestTransaction.end();

// Verify that the customer no longer exists
        assertFalse(customerRepository.existsById(response.id()));
    }
    @Test
    @Transactional
    void createCustomer_whenEmailAlreadyExists_throwsException() {

        // ARRANGE: Generate a unique email
        String email = "marco-" + UUID.randomUUID() + "@example.com";

        CreateCustomerRequest firstRequest = new CreateCustomerRequest(
                "Marco Bianchi",
                email
        );

        // Create the first customer in the real database
        customerService.createCustomer(firstRequest);

        // Prepare a second customer with the same email
        CreateCustomerRequest secondRequest = new CreateCustomerRequest(
                "Another Customer",
                email
        );

        // ACT AND ASSERT: The second registration must fail
        assertThrows(
                DuplicateResourceException.class,
                () -> customerService.createCustomer(secondRequest)
        );
    }

    @Test
    @Transactional
    void getCustomerById_whenCustomerExists_returnsCustomer() {

        // ARRANGE: Create a customer with a unique email
        String email = "sara-" + UUID.randomUUID() + "@example.com";

        CreateCustomerRequest request = new CreateCustomerRequest(
                "Sara Rossi",
                email
        );

        CustomerResponse createdCustomer =
                customerService.createCustomer(request);

        UUID customerId = createdCustomer.id();

        // ACT: Retrieve the customer from the real database
        CustomerResponse retrievedCustomer =
                customerService.getCustomer(customerId);

        // ASSERT: Verify the retrieved customer
        assertNotNull(retrievedCustomer);

        assertEquals(customerId, retrievedCustomer.id());

        assertEquals("Sara Rossi", retrievedCustomer.fullName());

        assertEquals(email, retrievedCustomer.email());
    }
    @Test
    void getCustomer_whenCustomerDoesNotExist_throwsException() {

        // ARRANGE: Generate a UUID
        UUID nonExistingId = UUID.randomUUID();

        // ACT & ASSERT: Verify the exception
        assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.getCustomer(nonExistingId)
        );
    }
    @Test
    @Transactional
    void updateCustomer_whenCustomerExists_updatesDatabase() {

        // ARRANGE: Create a customer
        String originalEmail =
                "sara-" + UUID.randomUUID() + "@example.com";

        CreateCustomerRequest createRequest =
                new CreateCustomerRequest(
                        "Sara Rossi",
                        originalEmail
                );

        CustomerResponse createdCustomer =
                customerService.createCustomer(createRequest);

        UUID customerId = createdCustomer.id();

        // Prepare updated information
        String updatedEmail =
                "sara.updated-" + UUID.randomUUID() + "@example.com";

        UpdateCustomerRequest updateRequest =
                new UpdateCustomerRequest(
                        "Sara Bianchi",
                        updatedEmail
                );

        // ACT: Update the customer
        CustomerResponse updatedCustomer =
                customerService.updateCustomer(customerId, updateRequest);

        // ASSERT: Verify the update response
        assertEquals(customerId, updatedCustomer.id());

        assertEquals("Sara Bianchi", updatedCustomer.fullName());

        assertEquals(updatedEmail, updatedCustomer.email());

        // ASSERT: Retrieve the customer again from the database
        CustomerResponse retrievedCustomer =
                customerService.getCustomer(customerId);

        assertEquals("Sara Bianchi", retrievedCustomer.fullName());

        assertEquals(updatedEmail, retrievedCustomer.email());
    }
    @Test
    @Transactional
    void updateCustomer_whenEmailBelongsToAnotherCustomer_throwsException() {

        // ARRANGE: Generate two unique emails
        String marcoEmail = "marco-" + UUID.randomUUID() + "@example.com";
        String saraEmail = "sara-" + UUID.randomUUID() + "@example.com";

        // Create Marco
        CreateCustomerRequest marcoRequest = new CreateCustomerRequest(
                "Marco Bianchi",
                marcoEmail
        );

        customerService.createCustomer(marcoRequest);

        // Create Sara
        CreateCustomerRequest saraRequest = new CreateCustomerRequest(
                "Sara Rossi",
                saraEmail
        );

        CustomerResponse sara = customerService.createCustomer(saraRequest);

        // Sara tries to use Marco's email
        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest(
                "Sara Rossi",
                marcoEmail
        );

        // ACT & ASSERT: The update must be rejected
        assertThrows(
                DuplicateResourceException.class,
                () -> customerService.updateCustomer(sara.id(), updateRequest)
        );

        // Verify Sara's original email is unchanged
        CustomerResponse unchangedSara = customerService.getCustomer(sara.id());

        assertEquals(saraEmail, unchangedSara.email());
    }
}