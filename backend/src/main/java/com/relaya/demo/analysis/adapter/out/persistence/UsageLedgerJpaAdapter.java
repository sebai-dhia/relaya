package com.relaya.demo.analysis.adapter.out.persistence;

import com.relaya.demo.analysis.port.out.UsageLedgerPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Component
public class UsageLedgerJpaAdapter implements UsageLedgerPort {

	private final SpringDataUsageLedgerRepository repository;

	public UsageLedgerJpaAdapter(SpringDataUsageLedgerRepository repository) {
		this.repository = Objects.requireNonNull(repository, "repository must not be null");
	}

	@Override
	public void recordUsage(
			UUID tenantId,
			UUID intakeId,
			String provider,
			String model,
			int promptTokens,
			int completionTokens,
			BigDecimal estimatedCostUsd
	) {
		Objects.requireNonNull(tenantId, "tenantId must not be null");
		Objects.requireNonNull(provider, "provider must not be null");
		Objects.requireNonNull(model, "model must not be null");
		BigDecimal cost = (estimatedCostUsd == null) ? BigDecimal.ZERO : estimatedCostUsd;

		UsageLedgerJpaEntity entity = new UsageLedgerJpaEntity(
				UUID.randomUUID(),
				tenantId,
				intakeId,
				provider,
				model,
				promptTokens,
				completionTokens,
				cost,
				Instant.now()
		);
		repository.save(entity);
	}

	@Override
	public BigDecimal getMonthToDateCostUsd(UUID tenantId, YearMonth month) {
		if (tenantId == null) {
			return BigDecimal.ZERO;
		}
		YearMonth ym = (month == null) ? YearMonth.now(ZoneOffset.UTC) : month;
		Instant start = ym.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant end = ym.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);

		BigDecimal sum = repository.sumCostByTenantIdAndDateRange(tenantId, start, end);
		return (sum == null) ? BigDecimal.ZERO : sum;
	}
}