package com.relaya.demo.analysis.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpringDataUsageLedgerRepository extends JpaRepository<UsageLedgerJpaEntity, UUID> {

	@Query("SELECT COALESCE(SUM(u.estimatedCostUsd), 0) FROM UsageLedgerJpaEntity u " +
			"WHERE u.tenantId = :tenantId AND u.calledAt >= :start AND u.calledAt < :end")
	BigDecimal sumCostByTenantIdAndDateRange(
			@Param("tenantId") UUID tenantId,
			@Param("start") Instant start,
			@Param("end") Instant end
	);

	List<UsageLedgerJpaEntity> findAllByTenantIdAndCalledAtGreaterThanEqual(UUID tenantId, Instant start);
}