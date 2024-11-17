package com.staking.stakingservice.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.staking.stakingservice.domain.entity.OnchainTransaction;
import com.staking.stakingservice.domain.enums.TransactionStatus;

@Repository
public interface OnchainTransactionRepository extends JpaRepository<OnchainTransaction, Long> {
    List<OnchainTransaction> findByCoinSymbolAndStatus(String coinSymbol, TransactionStatus status);

    Optional<OnchainTransaction> findByTxHash(String txHash);
}