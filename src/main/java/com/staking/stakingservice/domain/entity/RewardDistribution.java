package com.staking.stakingservice.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.staking.stakingservice.domain.enums.DistributionStatus;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reward_distributions")
@Getter
@Setter
public class RewardDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", nullable = false)
    private DailyRewardSummary summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "coin_symbol", nullable = false, length = 20)
    private String coinSymbol;

    @Column(name = "reward_calculated", nullable = false, precision = 30, scale = 18)
    private BigDecimal rewardCalculated;

    @Column(name = "reward_distributed", nullable = false, precision = 30, scale = 8)
    private BigDecimal rewardDistributed;

    @Column(name = "dust_amount", nullable = false, precision = 30, scale = 18)
    private BigDecimal dustAmount;

    @Column(name = "snapshot_balance", nullable = false, precision = 30, scale = 8)
    private BigDecimal snapshotBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DistributionStatus status;

    @Column(name = "batch_id", nullable = false)
    private Integer batchId;

    @Column(name = "distributed_at")
    private Instant distributedAt;

    @Column(name = "failed_reason")
    private String failedReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
