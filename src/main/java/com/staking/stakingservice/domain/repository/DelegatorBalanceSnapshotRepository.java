package com.staking.stakingservice.domain.repository;

import com.staking.stakingservice.domain.entity.DelegatorBalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DelegatorBalanceSnapshotRepository extends JpaRepository<DelegatorBalanceSnapshot, Long> {

    Optional<DelegatorBalanceSnapshot> findByCoinSymbolAndWalletAddressAndBatchId(
            String coinSymbol,
            String walletAddress,
            Integer batchId);

    // 특정 코인의 가장 최근 스냅샷 조회
    Optional<DelegatorBalanceSnapshot> findFirstByCoinSymbolAndWalletAddressOrderByBatchIdDesc(
            String coinSymbol,
            String walletAddress);

    // 특정 배치ID 이전의 가장 최근 스냅샷 조회
    Optional<DelegatorBalanceSnapshot> findFirstByCoinSymbolAndWalletAddressAndBatchIdLessThanOrderByBatchIdDesc(
            String coinSymbol,
            String walletAddress,
            Integer batchId);
}