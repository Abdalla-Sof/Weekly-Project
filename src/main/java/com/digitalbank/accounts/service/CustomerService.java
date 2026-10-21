
package com.digitalbank.accounts.service;

import com.digitalbank.accounts.dto.CreateCustomerRequest;
import com.digitalbank.accounts.dto.CustomerResponse;
import com.digitalbank.accounts.entity.Customer;
import com.digitalbank.accounts.exception.DuplicateResourceException;
import com.digitalbank.accounts.exception.ResourceNotFoundException;
import com.digitalbank.accounts.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(
            CreateCustomerRequest request) {

        String email = request.email().trim().toLowerCase();

        if (customerRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "A customer with this email already exists.");
        }

        Customer customer = new Customer(
                request.fullName().trim(), email);

        Customer saved = customerRepository.save(customer);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + id));

        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getCreatedAt()
        );
    }
}
