
package com.digitalbank.loans.service;

import com.digitalbank.loans.dto.CreateLoanRequest;
import com.digitalbank.loans.dto.LoanResponse;
import com.digitalbank.loans.entity.Loan;
import com.digitalbank.loans.repository.LoanRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class LoanService {

    private final LoanRepository loanRepository;

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Transactional
    public LoanResponse createLoan(CreateLoanRequest request) {

        String loanNumber = "LOAN-" + UUID.randomUUID();

        Loan loan = new Loan(
                request.customerId(),
                loanNumber,
                request.amount()
        );

        Loan saved = loanRepository.save(loan);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanResponse getLoan(UUID loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loan not found: " + loanId
                ));

        return toResponse(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByCustomer(UUID customerId) {

        return loanRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LoanResponse toResponse(Loan loan) {

        return new LoanResponse(
                loan.getId(),
                loan.getCustomerId(),
                loan.getLoanNumber(),
                loan.getAmount(),
                loan.getStatus(),
                loan.getCreatedAt()
        );
    }
}
