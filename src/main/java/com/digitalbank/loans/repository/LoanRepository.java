
package com.digitalbank.loans.repository;

import com.digitalbank.loans.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    List<Loan> findByCustomerId(UUID customerId);
}
