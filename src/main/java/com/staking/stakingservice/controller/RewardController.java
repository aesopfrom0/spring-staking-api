package com.staking.stakingservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.staking.stakingservice.controller.request.RewardConfirmationRequest;
import com.staking.stakingservice.controller.response.RewardConfirmationResponse;
import com.staking.stakingservice.domain.entity.DailyRewardSummary;
import com.staking.stakingservice.service.RewardConfirmationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardConfirmationService rewardConfirmationService;

    @PostMapping("/confirm")
    public ResponseEntity<RewardConfirmationResponse> confirmReward(
            @Valid @RequestBody RewardConfirmationRequest request) {

        log.info("리워드 확정 요청: {}", request);

        DailyRewardSummary summary = rewardConfirmationService.confirmReward(
                request.getCoinSymbol(),
                request.getBatchId(),
                request.getDelegatorAddress());

        return ResponseEntity.ok(RewardConfirmationResponse.from(summary));
    }
}