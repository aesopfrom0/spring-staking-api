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
import com.staking.stakingservice.domain.repository.StakingConfigRepository;
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
    private final StakingConfigRepository stakingConfigRepository;
    private final BlockchainProviderFactory blockchainProviderFactory;

    public DailyRewardSummary confirmReward(String coinSymbol, Integer batchId, String delegatorAddress) {
        // 요약 조회 또는 생성
        DailyRewardSummary summary = summaryRepository.findByCoinSymbolAndBatchId(coinSymbol, batchId)
                .orElseGet(() -> createNewSummary(coinSymbol, batchId));

        if (summary.getStatus() != RewardStatus.PENDING) {
            throw new IllegalStateException("리워드가 계산 완료 상태가 아닙니다: " + summary.getStatus());
        }

        try {
            BlockchainProvider provider = blockchainProviderFactory.getProvider(coinSymbol);

            if (summary.getClaimType() == ClaimType.INSTANT) {
                // TODO: 온체인 트랜잭션으로 실제 리워드 회수
            } else if (summary.getClaimType() == ClaimType.DEFERRED) {
                // 이전 스냅샷과 현재 잔고를 비교하여 리워드 계산
                DelegatorBalanceInfo currentBalance = provider.getDelegatorBalanceInfo(delegatorAddress);
                DelegatorBalanceSnapshot previousSnapshot = delegatorSnapshotRepository
                        .findFirstByCoinSymbolAndWalletAddressAndBatchIdLessThanOrderByBatchIdDesc(
                                coinSymbol,
                                delegatorAddress,
                                batchId)
                        .orElseGet(() -> {
                            log.info("[{}] 이전 스냅샷 없음. 최초 스테이킹으로 간주합니다. delegator: {}, batchId: {}",
                                    coinSymbol, delegatorAddress, batchId);

                            return DelegatorBalanceSnapshot.builder()
                                    .coinSymbol(coinSymbol)
                                    .walletAddress(delegatorAddress)
                                    .totalBalance(BigDecimal.ZERO)
                                    .stakingBalance(BigDecimal.ZERO)
                                    .rewardBalance(BigDecimal.ZERO)
                                    .liquidBalance(BigDecimal.ZERO)
                                    .batchId(batchId - 1) // 이전 날짜의 스냅샷인 것처럼
                                    .build();
                        });
                // 리워드는 현재 잔고와 이전 스냅샷의 차이
                BigDecimal rewardAmount = calculateRewardAmount(currentBalance, previousSnapshot);
                summary.setClaimedRewardAmount(rewardAmount);
            }

            // 상태 업데이트
            summary.setStatus(RewardStatus.AMOUNT_CONFIRMED);
            summary.setIsClaimed(true);
            summaryRepository.save(summary);

            log.info("[{}] 배치 ID {} 리워드 확정 완료. 금액: {}, 타입: {}, 주소: {}",
                    coinSymbol, batchId, summary.getClaimedRewardAmount(), summary.getClaimType(), delegatorAddress);

            return summary;

        } catch (Exception e) {
            summary.setStatus(RewardStatus.FAILED);
            summary.setErrorMessage(e.getMessage());
            summaryRepository.save(summary);

            log.error("[{}] 배치 ID {} 리워드 확정 실패. 주소: {}", coinSymbol, batchId, delegatorAddress, e);
            throw new RuntimeException("리워드 확정 중 오류 발생", e);
        }
    }

    private DailyRewardSummary createNewSummary(String coinSymbol, Integer batchId) {
        DailyRewardSummary newSummary = new DailyRewardSummary();
        newSummary.setCoinSymbol(coinSymbol);
        newSummary.setBatchId(batchId);
        newSummary.setStatus(RewardStatus.PENDING);
        newSummary.setIsClaimed(false);

        // 코인별 설정 조회하여 claim type 설정
        StakingConfig config = stakingConfigRepository.findByCoinSymbol(coinSymbol)
                .orElseThrow(() -> new IllegalArgumentException("스테이킹 설정을 찾을 수 없습니다: " + coinSymbol));
        newSummary.setClaimType(config.getClaimType());

        log.info("[{}] 배치 ID {} 리워드 요약 새로 생성됨", coinSymbol, batchId);
        return summaryRepository.save(newSummary);
    }

    private BigDecimal calculateRewardAmount(DelegatorBalanceInfo currentBalance,
            DelegatorBalanceSnapshot previousSnapshot) {
        // 리워드 잔고가 있다면 그것을 우선 사용
        if (currentBalance.rewardBalance() != null) {
            return currentBalance.rewardBalance();
        }

        // 전체 잔고의 차이를 계산
        BigDecimal difference = currentBalance.totalBalance()
                .subtract(previousSnapshot.getTotalBalance());

        // 잔고가 감소했거나 변화가 없으면 0 반환
        if (difference.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("리워드 없음. 현재 잔고: {}, 이전 잔고: {}",
                    currentBalance.totalBalance(),
                    previousSnapshot.getTotalBalance());
            return BigDecimal.ZERO;
        }

        return difference;
    }
}