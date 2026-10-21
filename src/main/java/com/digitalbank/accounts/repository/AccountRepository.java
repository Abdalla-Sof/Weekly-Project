
package com.digitalbank.accounts.repository;

import com.digitalbank.accounts.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    List<Account> findByCustomer_Id(UUID customerId);
}
