package com.staking.stakingservice.exception;

public class StakingConfigNotFoundException extends RuntimeException {
    public StakingConfigNotFoundException(String coinSymbol) {
        super(String.format("코인 %s에 대한 스테이킹 설정을 찾을 수 없습니다.", coinSymbol));
    }
}
