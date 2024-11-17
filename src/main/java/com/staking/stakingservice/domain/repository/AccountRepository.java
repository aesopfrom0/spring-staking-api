package com.staking.stakingservice.domain.repository;

import com.staking.stakingservice.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountRepository
        extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
}