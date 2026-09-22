package com.relaya.demo.analysis.adapter.out.ai;

import com.relaya.demo.analysis.port.out.UsageLedgerPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class BudgetGuardTest {

	private final UsageLedgerPort usageLedgerPort = Mockito.mock(UsageLedgerPort.class);
	private final BudgetGuard budgetGuard = new BudgetGuard(usageLedgerPort, 4000, new BigDecimal("5.00"));

	@Test
	@DisplayName("Allows call within token limit and monthly budget")
	void shouldAllowCallWithinGuards() {
		UUID tenantId = UUID.randomUUID();
		when(usageLedgerPort.getMonthToDateCostUsd(eq(tenantId), any(YearMonth.class)))
				.thenReturn(new BigDecimal("1.25"));

		assertDoesNotThrow(() -> budgetGuard.checkGuards(tenantId, 1500));
	}

	@Test
	@DisplayName("Rejects call exceeding per-call token limit")
	void shouldRejectCallExceedingTokenLimit() {
		UUID tenantId = UUID.randomUUID();
		assertThrows(BudgetExceededException.class, () -> budgetGuard.checkGuards(tenantId, 4500));
	}

	@Test
	@DisplayName("Rejects call when monthly budget reached or exceeded")
	void shouldRejectCallWhenMonthlyBudgetReached() {
		UUID tenantId = UUID.randomUUID();
		when(usageLedgerPort.getMonthToDateCostUsd(eq(tenantId), any(YearMonth.class)))
				.thenReturn(new BigDecimal("5.00"));

		assertThrows(BudgetExceededException.class, () -> budgetGuard.checkGuards(tenantId, 500));
	}
}