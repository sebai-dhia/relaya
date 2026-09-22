package com.relaya.demo.analysis.port.out;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public interface UsageLedgerPort {

	void recordUsage(
			UUID tenantId,
			UUID intakeId,
			String provider,
			String model,
			int promptTokens,
			int completionTokens,
			BigDecimal estimatedCostUsd
	);

	BigDecimal getMonthToDateCostUsd(UUID tenantId, YearMonth month);
}