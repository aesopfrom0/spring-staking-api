package com.staking.stakingservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.staking.stakingservice.common.util.DateTimeUtils;
import com.staking.stakingservice.domain.entity.Account;
import com.staking.stakingservice.domain.entity.BalanceSnapshot;
import com.staking.stakingservice.domain.entity.DailyBalanceSummary;
import com.staking.stakingservice.domain.repository.BalanceSnapshotRepository;
import com.staking.stakingservice.domain.repository.DailyBalanceSummaryRepository;
import com.staking.stakingservice.service.dto.request.BalanceSnapshotRequest;
import com.staking.stakingservice.service.dto.response.BalanceSnapshotResponse;
import com.staking.stakingservice.service.dto.response.BalanceSnapshotResponse.CoinSummary;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceSnapshotService {
    private static final List<String> COIN_SYMBOLS = List.of("SUI", "KRWX", "USDY", "JPYZ");
    private static final Random RANDOM = new Random();

    private final BalanceSnapshotRepository balanceSnapshotRepository;
    private final DailyBalanceSummaryRepository dailyBalanceSummaryRepository;
    private final AccountService accountService;

    @Transactional
    public BalanceSnapshotResponse createSnapshots(List<BalanceSnapshotRequest> requests, Integer batchId) {
        // batchId가 null이면 현재 날짜로 생성
        Integer finalBatchId = (batchId != null) ? batchId : DateTimeUtils.generateBatchId();

        // 스냅샷 생성
        for (BalanceSnapshotRequest request : requests) {
            try {
                Account account = accountService.findStakingEnabledAccountById(request.getAccountId());

                BalanceSnapshot snapshot = new BalanceSnapshot(
                        account,
                        request.getCoinSymbol(),
                        request.getBalance(),
                        finalBatchId);
                balanceSnapshotRepository.save(snapshot);
            } catch (Exception e) {
                log.error("계정 {}의 스냅샷 생성 실패: {}",
                        request.getAccountId(), e.getMessage(), e);
                throw new RuntimeException("잔고 스냅샷 생성 실패", e);
            }
        }

        // 코인별 집계 및 저장
        createSummaries(finalBatchId);

        // 생성된 결과 조회
        List<DailyBalanceSummary> summaries = dailyBalanceSummaryRepository
                .findByBatchId(finalBatchId);

        List<CoinSummary> coinSummaries = summaries.stream()
                .map(s -> new CoinSummary(
                        s.getCoinSymbol(),
                        s.getTotalSnapshotBalance(),
                        s.getAccountCount()))
                .collect(Collectors.toList());

        log.info("배치 {}에 대해 {}개의 스냅샷과 요약 데이터 생성 완료",
                finalBatchId, requests.size());

        return new BalanceSnapshotResponse(
                finalBatchId,
                (int) requests.stream().map(BalanceSnapshotRequest::getAccountId).distinct().count(),
                requests.size(),
                coinSummaries);
    }

    @Transactional
    public BalanceSnapshotResponse createRandomSnapshots(Integer batchId) {
        Integer finalBatchId = (batchId != null) ? batchId : DateTimeUtils.generateBatchId();
        List<Account> stakingAccounts = accountService.findStakingEnabledAccounts();
        List<BalanceSnapshotRequest> requests = new ArrayList<>();

        for (Account account : stakingAccounts) {
            for (String coinSymbol : COIN_SYMBOLS) {
                // TODO: 실제 구현 시에는 account_balance 테이블과 JOIN하여 실시간 잔고 조회 필요
                // 현재는 테스트를 위해 각 코인의 특성을 고려한 임의의 잔고 범위 설정
                BigDecimal balance = switch (coinSymbol) {
                    case "SUI" -> randomBalance(0, 100000);
                    case "KRWX" -> randomBalance(0, 10000000);
                    case "USDY" -> randomBalance(0, 100000);
                    case "JPYZ" -> randomBalance(0, 5000000);
                    default -> BigDecimal.ZERO;
                };

                requests.add(new BalanceSnapshotRequest(account.getId(), coinSymbol, balance));
            }
        }

        // 스냅샷 생성
        return createSnapshots(requests, finalBatchId);
    }

    private BigDecimal randomBalance(int min, int max) {
        // 정수 부분 랜덤 생성
        BigDecimal integerPart = new BigDecimal(RANDOM.nextInt(max - min + 1) + min);
        // 소수점 8자리까지 랜덤 생성
        BigDecimal decimalPart = new BigDecimal(RANDOM.nextInt(100000000))
                .divide(new BigDecimal("100000000"), 8, RoundingMode.DOWN);
        return integerPart.add(decimalPart);
    }

    private void createSummaries(Integer batchId) {
        // 코인별로 그룹화하여 집계
        Map<String, List<BalanceSnapshot>> snapshotsByCoin = balanceSnapshotRepository.findByBatchId(batchId)
                .stream()
                .collect(Collectors.groupingBy(BalanceSnapshot::getCoinSymbol));

        // 각 코인별로 집계 데이터 생성
        for (Map.Entry<String, List<BalanceSnapshot>> entry : snapshotsByCoin.entrySet()) {
            String coinSymbol = entry.getKey();
            List<BalanceSnapshot> snapshots = entry.getValue();

            BigDecimal totalBalance = snapshots.stream()
                    .map(BalanceSnapshot::getSnapshotBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int accountCount = (int) snapshots.stream()
                    .map(snapshot -> snapshot.getAccount().getId())
                    .distinct()
                    .count();

            DailyBalanceSummary summary = new DailyBalanceSummary(
                    coinSymbol,
                    batchId,
                    totalBalance,
                    accountCount);

            try {
                dailyBalanceSummaryRepository.save(summary);
                log.info("일별 잔고 요약 생성 완료 - 코인: {}, 배치: {}, 총잔고: {}, 계정수: {}",
                        coinSymbol, batchId, totalBalance, accountCount);
            } catch (Exception e) {
                log.error("코인 {}의 일별 잔고 요약 생성 실패: {}",
                        coinSymbol, e.getMessage(), e);
            }
        }
    }
}
