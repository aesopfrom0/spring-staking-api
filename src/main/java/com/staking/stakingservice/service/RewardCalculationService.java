package com.staking.stakingservice.service;

import com.staking.stakingservice.domain.entity.*;
import com.staking.stakingservice.domain.enums.DistributionStatus;
import com.staking.stakingservice.domain.enums.RewardStatus;
import com.staking.stakingservice.domain.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RewardCalculationService {
    private final StakingConfigService stakingConfigService;

    private final DailyRewardSummaryRepository summaryRepository;
    private final BalanceSnapshotRepository snapshotRepository;
    private final RewardDistributionRepository distributionRepository;

    private static final int CALCULATION_SCALE = 18;
    private static final int DISTRIBUTION_SCALE = 8;

    private record CalculationResult(
            BigDecimal totalCalculated,
            BigDecimal totalDistributed,
            BigDecimal totalDust,
            List<RewardDistribution> distributions) {
    }

    public DailyRewardSummary calculateRewards(String coinSymbol, Integer batchId) {
        DailyRewardSummary summary = getSummary(coinSymbol, batchId);
        StakingConfig config = stakingConfigService.getConfig(coinSymbol);
        List<BalanceSnapshot> snapshots = getSnapshots(coinSymbol, batchId);

        BigDecimal totalBalance = calculateTotalBalance(snapshots);
        CalculationResult result = calculateDistributions(summary, snapshots, totalBalance);

        return updateSummary(summary, config, totalBalance, snapshots.size(), result);
    }

    private DailyRewardSummary getSummary(String coinSymbol, Integer batchId) {
        DailyRewardSummary summary = summaryRepository.findByCoinSymbolAndBatchId(coinSymbol, batchId)
                .orElseThrow(() -> new IllegalArgumentException("리워드 요약을 찾을 수 없습니다."));

        if (summary.getStatus() != RewardStatus.AMOUNT_CONFIRMED) {
            throw new IllegalStateException("리워드 금액이 확정되지 않았습니다: " + summary.getStatus());
        }
        return summary;
    }

    private List<BalanceSnapshot> getSnapshots(String coinSymbol, Integer batchId) {
        List<BalanceSnapshot> snapshots = snapshotRepository.findAllByCoinSymbolAndBatchId(coinSymbol, batchId);
        if (snapshots.isEmpty()) {
            throw new IllegalStateException("스냅샷 데이터가 없습니다.");
        }
        return snapshots;
    }

    private BigDecimal calculateTotalBalance(List<BalanceSnapshot> snapshots) {
        return snapshots.stream()
                .map(BalanceSnapshot::getSnapshotBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CalculationResult calculateDistributions(
            DailyRewardSummary summary,
            List<BalanceSnapshot> snapshots,
            BigDecimal totalBalance) {

        BigDecimal totalCalculated = BigDecimal.ZERO;
        BigDecimal totalDistributed = BigDecimal.ZERO;
        List<RewardDistribution> distributions = new ArrayList<>();

        for (BalanceSnapshot snapshot : snapshots) {
            BigDecimal ratio = snapshot.getSnapshotBalance()
                    .divide(totalBalance, CALCULATION_SCALE, RoundingMode.HALF_UP);

            BigDecimal calculated = summary.getClaimedRewardAmount()
                    .multiply(ratio)
                    .setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);

            BigDecimal distributed = calculated.setScale(DISTRIBUTION_SCALE, RoundingMode.FLOOR);
            BigDecimal dust = calculated.subtract(distributed);

            totalCalculated = totalCalculated.add(calculated);
            totalDistributed = totalDistributed.add(distributed);

            distributions.add(RewardDistribution.builder()
                    .summary(summary)
                    .account(snapshot.getAccount())
                    .coinSymbol(summary.getCoinSymbol())
                    .rewardCalculated(calculated)
                    .rewardDistributed(distributed)
                    .dustAmount(dust)
                    .snapshotBalance(snapshot.getSnapshotBalance())
                    .status(DistributionStatus.PENDING)
                    .batchId(summary.getBatchId())
                    .build());
        }

        distributionRepository.saveAll(distributions);
        return new CalculationResult(totalCalculated, totalDistributed,
                totalCalculated.subtract(totalDistributed), distributions);
    }

    private DailyRewardSummary updateSummary(DailyRewardSummary summary, StakingConfig config,
            BigDecimal totalBalance, int accountCount, CalculationResult result) {
        summary.setStatus(RewardStatus.CALCULATED);
        summary.setTotalSnapshotBalance(totalBalance);
        summary.setAccountCount(accountCount);
        summary.setTotalRewardCalculated(result.totalCalculated);
        summary.setTotalRewardDistributed(result.totalDistributed);
        summary.setDustAmount(result.totalDust);
        summary.setDailyRewardRate(config.getDailyRewardRate());
        return summaryRepository.save(summary);
    }
}