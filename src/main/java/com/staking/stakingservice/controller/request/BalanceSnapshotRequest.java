package com.staking.stakingservice.controller.request;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class BalanceSnapshotRequest {
    private Long accountId;
    private String coinSymbol;
    private BigDecimal balance;
}