package com.staking.stakingservice.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.staking.stakingservice.domain.entity.DailyBalanceSummary;
import java.util.List;

@Repository
public interface DailyBalanceSummaryRepository extends JpaRepository<DailyBalanceSummary, Long> {
    Optional<DailyBalanceSummary> findByCoinSymbolAndBatchId(String coinSymbol, Integer batchId);

    List<DailyBalanceSummary> findByBatchId(Integer batchId);
}
