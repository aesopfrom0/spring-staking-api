package com.staking.stakingservice.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

import com.staking.stakingservice.domain.enums.ClaimType;
import com.staking.stakingservice.domain.enums.RewardStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "daily_reward_summaries")
@Getter
@Setter
public class DailyRewardSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_symbol", nullable = false, length = 20)
    private String coinSymbol;

    @Column(name = "batch_id", nullable = false)
    private Integer batchId;

    @Column(name = "distribution_date", nullable = true)
    private LocalDate distributionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RewardStatus status;

    @Column(name = "is_claimed", nullable = false)
    private Boolean isClaimed = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false, length = 20)
    private ClaimType claimType = ClaimType.INSTANT;

    @Column(name = "claimed_reward_amount", precision = 30, scale = 8)
    private BigDecimal claimedRewardAmount;

    @Column(name = "total_reward_calculated", precision = 30, scale = 18)
    private BigDecimal totalRewardCalculated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onchain_tx_id")
    private OnchainTransaction onchainTransaction;

    @Column(name = "exchange_fee", precision = 30, scale = 8)
    private BigDecimal exchangeFee;

    @Column(name = "total_snapshot_balance", precision = 30, scale = 8)
    private BigDecimal totalSnapshotBalance;

    @Column(name = "daily_reward_rate", precision = 10, scale = 8)
    private BigDecimal dailyRewardRate;

    @Column(name = "account_count")
    private Integer accountCount;

    @Column(name = "calculated_at")
    private Instant calculatedAt;

    @Column(name = "total_reward_distributed", precision = 30, scale = 8)
    private BigDecimal totalRewardDistributed;

    @Column(name = "dust_amount", precision = 30, scale = 18)
    private BigDecimal dustAmount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
