package com.staking.stakingservice.provider.blockchain;

import com.staking.stakingservice.service.dto.DelegatorBalanceInfo;

public interface BlockchainProvider {
    // 체인별 대표 지갑 상세 잔고 조회
    DelegatorBalanceInfo getDelegatorBalanceInfo(String walletAddress);
}
