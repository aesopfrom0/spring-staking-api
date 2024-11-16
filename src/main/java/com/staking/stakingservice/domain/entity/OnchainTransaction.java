package com.staking.stakingservice.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.staking.stakingservice.domain.enums.TransactionStatus;
import com.staking.stakingservice.domain.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "onchain_transactions")
@Getter
@Setter
public class OnchainTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_symbol", nullable = false, length = 20)
    private String coinSymbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", nullable = false, length = 20)
    private TransactionType txType;

    @Column(nullable = false, precision = 30, scale = 8)
    private BigDecimal amount;

    @Column(name = "gas_fee", precision = 30, scale = 8)
    private BigDecimal gasFee;

    @Column(name = "fee_symbol", length = 20)
    private String feeSymbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "tx_hash", length = 100)
    private String txHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tx_detail", columnDefinition = "jsonb")
    private Map<String, Object> txDetail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
