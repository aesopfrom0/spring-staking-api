package com.staking.stakingservice.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RewardConfirmationRequest {
    @NotBlank(message = "코인 심볼은 필수입니다")
    private String coinSymbol;

    @NotNull(message = "배치 ID는 필수입니다")
    private Integer batchId;

    @NotBlank(message = "대표 지갑 주소는 필수입니다")
    private String delegatorAddress;
}