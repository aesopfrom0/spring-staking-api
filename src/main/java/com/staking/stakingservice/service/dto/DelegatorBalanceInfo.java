package com.staking.stakingservice.service.dto;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.lang.Nullable;

/**
 * 대표 지갑의 잔고 정보를 담는 DTO
 * totalBalance는 필수값이며, 나머지 필드들은 선택적입니다.
 * 체인별 특성에 따라 일부 잔고 정보는 null일 수 있습니다.
 */
public record DelegatorBalanceInfo(
        BigDecimal totalBalance,
        @Nullable BigDecimal stakingBalance,
        @Nullable BigDecimal rewardBalance,
        @Nullable BigDecimal liquidBalance,
        @Nullable Map<String, Object> metadata) {
    // Validator constructor
    public DelegatorBalanceInfo {
        if (totalBalance == null) {
            throw new IllegalArgumentException("""
                    totalBalance는 null일 수 없습니다.
                    모든 체인은 최소한 전체 잔고 정보를 제공해야 합니다.
                    """);
        }
    }

    // Static factory method for empty balance
    public static DelegatorBalanceInfo empty(BigDecimal totalBalance) {
        return new DelegatorBalanceInfo(
                totalBalance,
                null,
                null,
                null,
                Map.of());
    }

    // 체인별 메타데이터 조회 헬퍼 메서드
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        return metadata != null ? type.cast(metadata.get(key)) : null;
    }
}