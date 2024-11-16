package com.staking.stakingservice.domain.repository;

import com.staking.stakingservice.domain.entity.BalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BalanceSnapshotRepository extends JpaRepository<BalanceSnapshot, Long> {
    List<BalanceSnapshot> findByBatchId(Integer batchId);

    @Query("SELECT bs.coinSymbol as coinSymbol, " +
            "COUNT(DISTINCT bs.accountId) as accountCount, " +
            "SUM(bs.snapshotBalance) as totalSnapshotBalance " +
            "FROM BalanceSnapshot bs " +
            "WHERE bs.batchId = :batchId " +
            "GROUP BY bs.coinSymbol")
    List<BalanceSnapshotSummary> findSnapshotSummaryByBatchId(@Param("batchId") Integer batchId);

    interface BalanceSnapshotSummary {
        String getCoinSymbol();

        Long getAccountCount();

        BigDecimal getTotalSnapshotBalance();
    }
}
