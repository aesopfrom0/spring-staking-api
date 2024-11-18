package com.staking.stakingservice.service;

import org.springframework.stereotype.Service;

import com.staking.stakingservice.domain.entity.StakingConfig;
import com.staking.stakingservice.domain.repository.StakingConfigRepository;
import com.staking.stakingservice.exception.StakingConfigNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StakingConfigService {
    private final StakingConfigRepository stakingConfigRepository;

    public StakingConfig getConfig(String coinSymbol) {
        return stakingConfigRepository.findByCoinSymbol(coinSymbol)
                .orElseThrow(() -> new StakingConfigNotFoundException(coinSymbol));
    }

}
