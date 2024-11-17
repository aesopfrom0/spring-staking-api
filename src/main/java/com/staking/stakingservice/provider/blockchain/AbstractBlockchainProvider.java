package com.staking.stakingservice.provider.blockchain;

import java.math.BigDecimal;

import com.staking.stakingservice.service.dto.DelegatorBalanceInfo;
import com.staking.stakingservice.service.dto.RawBalanceInfo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractBlockchainProvider implements BlockchainProvider {

    @Override
    public DelegatorBalanceInfo getDelegatorBalanceInfo(String walletAddress) {
        // 1. 체인별 구체적인 잔고 정보 조회
        RawBalanceInfo rawBalance = fetchRawBalance(walletAddress);

        // 2. 공통 로직으로 total balance 계산
        BigDecimal totalBalance = calculateTotalBalance(rawBalance);

        // 3. DTO 구성
        return new DelegatorBalanceInfo(
                totalBalance,
                rawBalance.stakingBalance(),
                rawBalance.rewardBalance(),
                rawBalance.liquidBalance(),
                rawBalance.metadata());
    }

    // 체인별로 구현해야 하는 메서드
    protected abstract RawBalanceInfo fetchRawBalance(String walletAddress);

    // 공통 계산 로직
    protected BigDecimal calculateTotalBalance(RawBalanceInfo rawBalance) {
        BigDecimal total = BigDecimal.ZERO;

        if (rawBalance.stakingBalance() != null) {
            total = total.add(rawBalance.stakingBalance());
        }
        if (rawBalance.liquidBalance() != null) {
            total = total.add(rawBalance.liquidBalance());
        }
        if (rawBalance.rewardBalance() != null) {
            total = total.add(rawBalance.rewardBalance());
        }

        return total;
    }
}
