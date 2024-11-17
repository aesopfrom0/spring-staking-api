package com.staking.stakingservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.staking.stakingservice.domain.entity.Account;
import com.staking.stakingservice.domain.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public List<Account> findAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        log.debug("전체 계정 {}개 조회 완료", accounts.size());
        return accounts;
    }

    public Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "계정을 찾을 수 없습니다: " + accountId));
    }
}
