package com.staking.stakingservice.controller.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.staking.stakingservice.domain.entity.DailyRewardSummary;
import com.staking.stakingservice.domain.enums.ClaimType;
import com.staking.stakingservice.domain.enums.RewardStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RewardConfirmationResponse {
    private Long id;
    private String coinSymbol;
    private Integer batchId;
    private RewardStatus status;
    private ClaimType claimType;
    private Boolean isClaimed;
    private BigDecimal claimedRewardAmount;
    private BigDecimal totalRewardCalculated;
    private Instant completedAt;
    private String errorMessage;

    public static RewardConfirmationResponse from(DailyRewardSummary summary) {
        return RewardConfirmationResponse.builder()
                .id(summary.getId())
                .coinSymbol(summary.getCoinSymbol())
                .batchId(summary.getBatchId())
                .status(summary.getStatus())
                .claimType(summary.getClaimType())
                .isClaimed(summary.getIsClaimed())
                .claimedRewardAmount(summary.getClaimedRewardAmount())
                .totalRewardCalculated(summary.getTotalRewardCalculated())
                .completedAt(summary.getCompletedAt())
                .errorMessage(summary.getErrorMessage())
                .build();
    }
}