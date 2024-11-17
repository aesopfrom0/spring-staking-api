package com.staking.stakingservice.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.staking.stakingservice.domain.entity.Account;
import com.staking.stakingservice.domain.repository.AccountRepository;
import com.staking.stakingservice.domain.repository.AccountSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public List<Account> findStakingEnabledAccounts() {
        Specification<Account> spec = Specification.where(AccountSpecification.withStakingAgreed());
        List<Account> accounts = accountRepository.findAll(spec);
        log.info("스테이킹 가능한 계정 {}개 조회 완료", accounts.size());
        return accounts;
    }

    public Account findStakingEnabledAccountById(Long accountId) {
        Specification<Account> spec = Specification.where(AccountSpecification.withStakingAgreed());

        return accountRepository.findOne(spec.and((root, query, cb) -> cb.equal(root.get("id"), accountId)))
                .orElseThrow(() -> new IllegalArgumentException(
                        "스테이킹 가능한 계정을 찾을 수 없습니다: " + accountId));
    }
}
