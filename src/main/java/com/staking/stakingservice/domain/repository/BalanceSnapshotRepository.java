package com.staking.stakingservice.domain.repository;

import com.staking.stakingservice.domain.entity.BalanceSnapshot;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceSnapshotRepository extends JpaRepository<BalanceSnapshot, Long> {
    List<BalanceSnapshot> findByBatchId(Integer batchId);
}