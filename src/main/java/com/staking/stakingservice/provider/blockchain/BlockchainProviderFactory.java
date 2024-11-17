package com.staking.stakingservice.provider.blockchain;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlockchainProviderFactory {
    private final Map<String, BlockchainProvider> providers;

    public BlockchainProvider getProvider(String coinSymbol) {
        BlockchainProvider provider = providers.get(coinSymbol);
        if (provider == null) {
            throw new IllegalArgumentException("지원하지 않는 코인입니다: " + coinSymbol);
        }
        return provider;
    }
}
