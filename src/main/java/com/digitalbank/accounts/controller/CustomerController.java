
package com.digitalbank.accounts.controller;

import com.digitalbank.accounts.dto.CreateCustomerRequest;
import com.digitalbank.accounts.dto.CustomerResponse;
import com.digitalbank.accounts.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomer(@PathVariable UUID id) {
        return customerService.getCustomer(id);
    }
}
