package com.staking.stakingservice.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "delegator_balance_snapshots")
@Getter
@Builder(access = AccessLevel.PUBLIC)
public class DelegatorBalanceSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_symbol", nullable = false, length = 20)
    private String coinSymbol;

    @Column(name = "wallet_address", nullable = false, length = 100)
    private String walletAddress;

    @Column(name = "total_balance", nullable = false, precision = 30, scale = 8)
    private BigDecimal totalBalance;

    @Column(name = "staking_balance", precision = 30, scale = 8)
    private BigDecimal stakingBalance;

    @Column(name = "reward_balance", precision = 30, scale = 8)
    private BigDecimal rewardBalance;

    @Column(name = "liquid_balance", precision = 30, scale = 8)
    private BigDecimal liquidBalance;

    @Column(name = "chain_metadata", columnDefinition = "jsonb")
    private String chainMetadata; // JSONB 타입은 String으로 매핑

    @Column(name = "batch_id", nullable = false)
    private Integer batchId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
