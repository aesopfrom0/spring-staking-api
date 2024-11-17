package com.staking.stakingservice.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.staking.stakingservice.domain.entity.StakingConfig;

@Repository
public interface StakingConfigRepository extends JpaRepository<StakingConfig, Long> {

    Optional<StakingConfig> findByCoinSymbol(String coinSymbol);

    boolean existsByCoinSymbol(String coinSymbol);

    /**
     * 활성화된 스테이킹 설정만 조회
     */
    Optional<StakingConfig> findByCoinSymbolAndEnabledTrue(String coinSymbol);
}