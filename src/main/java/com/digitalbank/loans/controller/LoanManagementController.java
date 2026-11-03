
package com.digitalbank.loans.controller;

import com.digitalbank.loans.dto.CreateLoanRequest;
import com.digitalbank.loans.dto.LoanResponse;
import com.digitalbank.loans.service.LoanService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
public class LoanManagementController {

    private final LoanService loanService;

    public LoanManagementController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse createLoan(
            @Valid @RequestBody CreateLoanRequest request
    ) {
        return loanService.createLoan(request);
    }

    @GetMapping("/{loanId}")
    public LoanResponse getLoan(@PathVariable UUID loanId) {
        return loanService.getLoan(loanId);
    }

    @GetMapping("/customer/{customerId}")
    public List<LoanResponse> getLoansByCustomer(
            @PathVariable UUID customerId
    ) {
        return loanService.getLoansByCustomer(customerId);
    }
}
