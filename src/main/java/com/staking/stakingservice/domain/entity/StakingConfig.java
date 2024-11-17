package com.staking.stakingservice.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.staking.stakingservice.domain.enums.ClaimType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "staking_configs")
@Getter
@Setter
public class StakingConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_symbol", nullable = false, length = 20, unique = true)
    private String coinSymbol;

    @Column(name = "daily_reward_rate", nullable = false, precision = 10, scale = 8)
    private BigDecimal dailyRewardRate;

    @Column(name = "min_staking_amount", nullable = false, precision = 30, scale = 8)
    private BigDecimal minStakingAmount;

    @Column(name = "max_staking_amount", precision = 30, scale = 8)
    private BigDecimal maxStakingAmount;

    @Column(name = "total_staking_limit", precision = 30, scale = 8)
    private BigDecimal totalStakingLimit;

    @Column(name = "current_total_staked", nullable = false, precision = 30, scale = 8)
    private BigDecimal currentTotalStaked = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "unbonding_period", nullable = false)
    private Integer unbondingPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false, length = 20)
    private ClaimType claimType = ClaimType.INSTANT;

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
