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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "daily_balance_summaries")
@Getter
@Setter
public class DailyBalanceSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_symbol", nullable = false, length = 20)
    private String coinSymbol;

    @Column(name = "batch_id", nullable = false)
    private Integer batchId; // YYYYMMDD 형식

    @Column(name = "total_snapshot_balance", nullable = false, precision = 30, scale = 8)
    private BigDecimal totalSnapshotBalance;

    @Column(name = "account_count", nullable = false)
    private Integer accountCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
