package com.staking.stakingservice.domain.repository;

import org.springframework.data.jpa.domain.Specification;

import com.staking.stakingservice.domain.entity.Account;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
/**
 * NestJS에서의 동적 쿼리 처리:
 * 1. TypeORM QueryBuilder 사용 시:
 * this.accountRepository
 * .createQueryBuilder('account')
 * .where('account.isStakingTermsAgreed = :isAgreed', { isAgreed: true })
 * 
 * 2. Prisma 사용 시:
 * const { isStakingTermsAgreed } = queryDto;
 * const where = {
 * ...(!isNil(isStakingTermsAgreed) && {
 * isStakingTermsAgreed,
 * })
 * };
 * prisma.account.findMany({ where });
 */
public class AccountSpecification {
    public static Specification<Account> withStakingAgreed() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isStakingTermsAgreed"));
    }
}
