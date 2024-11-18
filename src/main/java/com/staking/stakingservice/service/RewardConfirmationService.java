package com.staking.stakingservice.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.staking.stakingservice.domain.entity.DailyRewardSummary;
import com.staking.stakingservice.domain.entity.DelegatorBalanceSnapshot;
import com.staking.stakingservice.domain.entity.StakingConfig;
import com.staking.stakingservice.domain.enums.ClaimType;
import com.staking.stakingservice.domain.enums.RewardStatus;
import com.staking.stakingservice.domain.repository.DailyRewardSummaryRepository;
import com.staking.stakingservice.domain.repository.DelegatorBalanceSnapshotRepository;
import com.staking.stakingservice.provider.blockchain.BlockchainProvider;
import com.staking.stakingservice.provider.blockchain.BlockchainProviderFactory;
import com.staking.stakingservice.service.dto.DelegatorBalanceInfo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RewardConfirmationService {
    private final DailyRewardSummaryRepository summaryRepository;
    private final DelegatorBalanceSnapshotRepository delegatorSnapshotRepository;
    private final StakingConfigService stakingConfigService;
    private final BlockchainProviderFactory blockchainProviderFactory;

    public DailyRewardSummary confirmReward(String coinSymbol, Integer batchId, String delegatorAddress) {
        log.info(coinSymbol);
        DailyRewardSummary summary = getSummary(coinSymbol, batchId);
        log.info("[Confirm Reward] 요약 조회 완료 - 상태: {}", summary.getStatus());
        validateSummaryStatus(summary);

        try {
            processSummaryReward(summary, coinSymbol, batchId, delegatorAddress);
            DailyRewardSummary updated = updateSummaryStatus(summary);
            log.info("[Confirm Reward] 상태 업데이트 완료 - 새 상태: {}", updated.getStatus());
            return updated;
        } catch (Exception e) {
            log.error("[Confirm Reward] 처리 중 에러 발생", e);
            return handleConfirmationError(summary, delegatorAddress, e);
        }
    }

    private DailyRewardSummary getSummary(String coinSymbol, Integer batchId) {
        return summaryRepository.findByCoinSymbolAndBatchId(coinSymbol, batchId)
                .orElseGet(() -> createNewSummary(coinSymbol, batchId));
    }

    private DailyRewardSummary createNewSummary(String coinSymbol, Integer batchId) {
        StakingConfig config = stakingConfigService.getConfig(coinSymbol);
        DailyRewardSummary summary = new DailyRewardSummary();
        summary.setCoinSymbol(coinSymbol);
        summary.setBatchId(batchId);
        summary.setStatus(RewardStatus.PENDING);
        summary.setClaimType(config.getClaimType());
        summary.setIsClaimed(false);
        summary.setClaimedRewardAmount(BigDecimal.ZERO);
        return summaryRepository.save(summary);
    }

    private void processSummaryReward(DailyRewardSummary summary, String coinSymbol,
            Integer batchId, String delegatorAddress) {
        BlockchainProvider provider = blockchainProviderFactory.getProvider(coinSymbol);

        if (summary.getClaimType() == ClaimType.INSTANT) {
            // TODO: 실제 블록체인 연동 시 구현 필요
            // processInstantClaim(provider, summary);
        } else {
            processDeferredClaim(provider, summary, coinSymbol, batchId, delegatorAddress);
        }
    }

    private void processDeferredClaim(BlockchainProvider provider, DailyRewardSummary summary,
            String coinSymbol, Integer batchId, String delegatorAddress) {
        DelegatorBalanceInfo currentBalance = provider.getDelegatorBalanceInfo(delegatorAddress);
        log.info("[Deferred Claim] 현재 잔고 조회 완료 - 총액: {}", currentBalance.totalBalance());

        DelegatorBalanceSnapshot previousSnapshot = getPreviousSnapshot(coinSymbol, delegatorAddress, batchId);
        log.info("[Deferred Claim] 이전 스냅샷 조회 완료 - 총액: {}", previousSnapshot.getTotalBalance());

        BigDecimal rewardAmount = calculateRewardAmount(currentBalance, previousSnapshot);
        log.info("[Deferred Claim] 리워드 계산 완료 - 금액: {}", rewardAmount);

        summary.setClaimedRewardAmount(rewardAmount);
        summaryRepository.save(summary);
        log.info("[Deferred Claim] 요약 정보 업데이트 완료");
    }

    private DelegatorBalanceSnapshot getPreviousSnapshot(String coinSymbol, String delegatorAddress, Integer batchId) {
        return delegatorSnapshotRepository
                .findFirstByCoinSymbolAndWalletAddressAndBatchIdLessThanOrderByBatchIdDesc(
                        coinSymbol, delegatorAddress, batchId)
                .orElseGet(() -> DelegatorBalanceSnapshot.builder()
                        .coinSymbol(coinSymbol)
                        .walletAddress(delegatorAddress)
                        .totalBalance(BigDecimal.ZERO)
                        .stakingBalance(BigDecimal.ZERO)
                        .rewardBalance(BigDecimal.ZERO)
                        .liquidBalance(BigDecimal.ZERO)
                        .batchId(batchId - 1)
                        .build());
    }

    private DailyRewardSummary updateSummaryStatus(DailyRewardSummary summary) {
        summary.setStatus(RewardStatus.AMOUNT_CONFIRMED);
        summary.setIsClaimed(summary.getClaimType() == ClaimType.INSTANT);
        return summaryRepository.save(summary);
    }

    private DailyRewardSummary handleConfirmationError(DailyRewardSummary summary,
            String delegatorAddress, Exception e) {
        summary.setStatus(RewardStatus.FAILED);
        summary.setErrorMessage(e.getMessage());
        summaryRepository.save(summary);
        log.error("[{}] 배치 ID {} 리워드 확정 실패. 주소: {}",
                summary.getCoinSymbol(), summary.getBatchId(), delegatorAddress, e);
        throw new RuntimeException("리워드 확정 중 오류 발생", e);
    }

    private BigDecimal calculateRewardAmount(DelegatorBalanceInfo currentBalance,
            DelegatorBalanceSnapshot previousSnapshot) {
        if (currentBalance.rewardBalance() != null) {
            return currentBalance.rewardBalance();
        }

        BigDecimal difference = currentBalance.totalBalance()
                .subtract(previousSnapshot.getTotalBalance());

        return difference.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO : difference;
    }

    private void validateSummaryStatus(DailyRewardSummary summary) {
        if (summary.getStatus() != RewardStatus.PENDING) {
            throw new IllegalStateException("리워드가 계산 완료 상태가 아닙니다: " + summary.getStatus());
        }
    }
}