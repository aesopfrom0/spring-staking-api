package com.staking.stakingservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.staking.stakingservice.service.BalanceSnapshotService;
import com.staking.stakingservice.service.dto.request.BalanceSnapshotRequest;
import com.staking.stakingservice.service.dto.response.BalanceSnapshotResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/balance-snapshots")
public class BalanceSnapshotController {
    private final BalanceSnapshotService balanceSnapshotService;

    @PostMapping("/random")
    public ResponseEntity<BalanceSnapshotResponse> createRandomSnapshots(
            @RequestParam(required = false) Integer batchId) {
        log.info("랜덤 잔고 스냅샷 생성" + (batchId != null ? " (배치ID: " + batchId + ")" : ""));

        BalanceSnapshotResponse response = balanceSnapshotService.createRandomSnapshots(batchId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    public ResponseEntity<BalanceSnapshotResponse> createSnapshots(
            @RequestBody List<BalanceSnapshotRequest> requests,
            @RequestParam(required = false) Integer batchId) {
        log.info("{}개의 잔고 스냅샷 생성" + (batchId != null ? " (배치ID: " + batchId + ")" : ""),
                requests.size());

        BalanceSnapshotResponse response = balanceSnapshotService.createSnapshots(requests, batchId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}