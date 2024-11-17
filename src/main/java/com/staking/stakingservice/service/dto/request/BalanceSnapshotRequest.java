package com.staking.stakingservice.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BalanceSnapshotRequest {
    private final Long accountId;
    private final String coinSymbol;
    private final BigDecimal balance;
}