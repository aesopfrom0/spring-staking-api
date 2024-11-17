package com.staking.stakingservice.service.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BalanceSnapshotResponse {
    private Integer batchId;
    private int totalAccounts;
    private int totalSnapshots;
    private List<CoinSummary> summaries;

    @Getter
    @AllArgsConstructor
    public static class CoinSummary {
        private String coinSymbol;
        private BigDecimal totalBalance;
        private int accountCount;
    }
}
