package com.staking.stakingservice.service.dto;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.annotation.Nullable;

public record RawBalanceInfo(
        @Nullable BigDecimal stakingBalance,
        @Nullable BigDecimal rewardBalance,
        @Nullable BigDecimal liquidBalance,
        Map<String, Object> metadata) {
}
