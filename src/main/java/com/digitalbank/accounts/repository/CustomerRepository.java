
package com.digitalbank.accounts.repository;

import com.digitalbank.accounts.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository
        extends JpaRepository<Customer, UUID> {

    boolean existsByEmail(String email);
}

