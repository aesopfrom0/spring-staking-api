package com.staking.stakingservice.provider.blockchain.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.staking.stakingservice.provider.blockchain.AbstractBlockchainProvider;
import com.staking.stakingservice.service.dto.RawBalanceInfo;

@Component("SUI")
public class SuiBlockchainProvider extends AbstractBlockchainProvider {
    @Override
    protected RawBalanceInfo fetchRawBalance(String walletAddress) {
        // TODO: SUI RPC 호출

        return new RawBalanceInfo(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null);
    }
}