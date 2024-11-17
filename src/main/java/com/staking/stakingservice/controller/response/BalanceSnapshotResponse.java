package com.staking.stakingservice.controller.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BalanceSnapshotResponse {
    private Integer batchId;
    private int totalAccounts;
    private int totalSnapshots;
    private List<CoinSummary> summaries;

    @Getter
    @Builder
    public static class CoinSummary {
        private String coinSymbol;
        private BigDecimal totalBalance;
        private int accountCount;
    }
}
