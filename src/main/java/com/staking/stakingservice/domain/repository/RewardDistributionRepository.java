package com.staking.stakingservice.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.staking.stakingservice.domain.entity.RewardDistribution;

@Repository
public interface RewardDistributionRepository extends JpaRepository<RewardDistribution, Long> {

}
