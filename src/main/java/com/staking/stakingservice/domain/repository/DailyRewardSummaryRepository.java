package com.staking.stakingservice.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.staking.stakingservice.domain.entity.DailyRewardSummary;

@Repository
public interface DailyRewardSummaryRepository extends JpaRepository<DailyRewardSummary, Long> {

    Optional<DailyRewardSummary> findByCoinSymbolAndBatchId(String coinSymbol, Integer batchId);

    List<DailyRewardSummary> findByBatchId(Integer batchId);
}
