package com.staking.stakingservice.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "balance_snapshots")
@Getter
public class BalanceSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "coin_symbol", nullable = false, length = 20)
    private String coinSymbol;

    @Column(name = "snapshot_balance", nullable = false, precision = 30, scale = 8)
    private BigDecimal snapshotBalance;

    @Column(name = "batch_id", nullable = false)
    private Integer batchId; // YYYYMMDD 형식

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public BalanceSnapshot(Account account, String coinSymbol, BigDecimal snapshotBalance, Integer batchId) {
        this.account = account;
        this.coinSymbol = coinSymbol;
        this.snapshotBalance = snapshotBalance;
        this.batchId = batchId;
    }
}
