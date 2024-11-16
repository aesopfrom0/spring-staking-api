-- 사용자 기본 정보
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    is_staking_terms_agreed BOOLEAN NOT NULL DEFAULT false,
    -- 본 프로젝트에서는 약관 동의 이력 관리를 생략하고 accounts 테이블에서만 현재 상태를 관리합니다.
    staking_terms_agreed_at TIMESTAMP,
    staking_terms_withdrawn_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(username)
);

-- 사용자별 리워드 정보
CREATE TABLE account_reward_info (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    coin_symbol VARCHAR(20) NOT NULL, -- coin 정보는 편의상 정규화 생략
    total_reward_amount DECIMAL(30,8) NOT NULL DEFAULT 0,
    last_reward_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(account_id, coin_symbol)
);

-- 잔고 스냅샷
CREATE TABLE balance_snapshots (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    coin_symbol VARCHAR(20) NOT NULL,  -- coin 정보는 편의상 정규화 생략
    snapshot_balance DECIMAL(30,8) NOT NULL,
    batch_id INTEGER NOT NULL,  -- YYYYMMDD 형식
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(account_id, coin_symbol, batch_id)
);

-- 일별 스냅샷 요약 테이블 (모니터링/집계 용도)
CREATE TABLE daily_balance_summaries (
    id BIGSERIAL PRIMARY KEY,
    coin_symbol VARCHAR(20) NOT NULL,
    batch_id INTEGER NOT NULL,  -- YYYYMMDD 형식
    total_snapshot_balance DECIMAL(30,8) NOT NULL,
    account_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(coin_symbol, batch_id)
);

-- 코인별 스테이킹 설정
CREATE TABLE staking_configs (
    id BIGSERIAL PRIMARY KEY,
    coin_symbol VARCHAR(20) NOT NULL,
    daily_reward_rate DECIMAL(10,8) NOT NULL,     -- 일 0.01% = 0.00010000, 연 3.65%
    min_staking_amount DECIMAL(30,8) NOT NULL,    -- 최소 스테이킹 금액
    max_staking_amount DECIMAL(30,8),             -- 개인별 최대 스테이킹 금액 (null이면 무제한)
    total_staking_limit DECIMAL(30,8),            -- 전체 스테이킹 한도 (null이면 무제한)
    current_total_staked DECIMAL(30,8) NOT NULL DEFAULT 0,  -- 현재 총 스테이킹된 금액
    enabled BOOLEAN NOT NULL DEFAULT true,
    unbonding_period INTEGER NOT NULL,            -- 언본딩 기간(일)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(coin_symbol)
);

-- 온체인 트랜잭션 기록
CREATE TABLE onchain_transactions (
    id BIGSERIAL PRIMARY KEY,
    coin_symbol VARCHAR(20) NOT NULL,
    tx_type VARCHAR(20) NOT NULL,              -- STAKE/UNSTAKE/REWARD_CLAIM
    amount DECIMAL(30,8) NOT NULL,
    gas_fee DECIMAL(30,8),                     -- 총 가스비 (gas_price * gas_used)
    fee_symbol VARCHAR(20),                    -- 가스비 지불 코인 심볼
    status VARCHAR(20) NOT NULL,               -- PENDING/SUCCESS/FAILED
    tx_hash VARCHAR(100),                      -- 트랜잭션 해시
    tx_detail JSONB,                           -- 가스 정보 등 상세 내역 (tx 자체 저장)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP                     -- 트랜잭션 완료 시점
);

-- 일일 보상 배치 작업 요약
CREATE TABLE daily_reward_summaries (
    id BIGSERIAL PRIMARY KEY,
    coin_symbol VARCHAR(20) NOT NULL,
    batch_id INTEGER NOT NULL,                       -- YYYYMMDD 형식
    distribution_date DATE NOT NULL,                 -- 보상 기준일
    status VARCHAR(20) NOT NULL,                     -- PENDING/CALCULATING/CALCULATED/DISTRIBUTING/COMPLETED/FAILED
    is_claimed BOOLEAN NOT NULL DEFAULT false,       -- 실제 리워드 회수 여부
    claim_type VARCHAR(20) NOT NULL DEFAULT 'INSTANT', -- INSTANT/DEFERRED 즉시/후불 회수 타입

    -- 1. 총 보상량 결정
    claimed_reward_amount DECIMAL(30,8),              -- 체인에서 실제 회수했거나, 회수 예정인 리워드 양
    total_reward_calculated DECIMAL(30,18),           -- 계산된 총 보상
    onchain_tx_id BIGINT REFERENCES onchain_transactions(id),  -- 온체인 트랜잭션 ID
    exchange_fee DECIMAL(30,8),                       -- 수수료

    -- 2. 스냅샷 & 계산
    total_snapshot_balance DECIMAL(30,8),             -- 스냅샷 시점 총 잔고
    daily_reward_rate DECIMAL(10,8),                  -- 실제 계산된 일일 보상률
    account_count INTEGER,                            -- 대상 계정 수
    calculated_at TIMESTAMP,                          -- 계산 완료 시점

    -- 3. 실제 분배
    total_reward_distributed DECIMAL(30,8),           -- 실제 분배된 보상
    dust_amount DECIMAL(30,18),                       -- 먼지 수량 (calculated - distributed)

    error_message TEXT,                               -- 실패 시 에러 메시지
    completed_at TIMESTAMP,                           -- 최종 완료 시점
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(coin_symbol, batch_id)
);

-- 개별 보상 내역
CREATE TABLE reward_distributions (
    id BIGSERIAL PRIMARY KEY,
    summary_id BIGINT NOT NULL REFERENCES daily_reward_summaries(id),
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    coin_symbol VARCHAR(20) NOT NULL,
    reward_calculated DECIMAL(30,18) NOT NULL,   -- 정밀 계산값
    reward_distributed DECIMAL(30,8) NOT NULL,   -- 실제 지급값
    dust_amount DECIMAL(30,18) NOT NULL,        -- 차이값 (calculated - distributed)
    snapshot_balance DECIMAL(30,8) NOT NULL,    -- 스냅샷 시점 잔고

    status VARCHAR(20) NOT NULL,                -- PENDING/DISTRIBUTED/FAILED
    batch_id INTEGER NOT NULL,                  -- summary의 batch_id와 동일
    distributed_at TIMESTAMP,
    failed_reason TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(account_id, coin_symbol, batch_id)
);