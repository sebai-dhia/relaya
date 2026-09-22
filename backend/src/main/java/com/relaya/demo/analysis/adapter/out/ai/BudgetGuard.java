package com.relaya.demo.analysis.adapter.out.ai;

import com.relaya.demo.analysis.port.out.UsageLedgerPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

@Component
public class BudgetGuard {

	private final UsageLedgerPort usageLedgerPort;
	private final int maxTokensPerCall;
	private final BigDecimal monthlyBudgetUsd;

	public BudgetGuard(
			UsageLedgerPort usageLedgerPort,
			@Value("${relaya.ai.max-tokens-per-call:4000}") int maxTokensPerCall,
			@Value("${relaya.ai.monthly-budget-usd:5.00}") BigDecimal monthlyBudgetUsd
	) {
		this.usageLedgerPort = Objects.requireNonNull(usageLedgerPort, "usageLedgerPort must not be null");
		this.maxTokensPerCall = maxTokensPerCall;
		this.monthlyBudgetUsd = monthlyBudgetUsd;
	}

	public void checkGuards(UUID tenantId, int estimatedPromptTokens) {
		Objects.requireNonNull(tenantId, "tenantId must not be null");

		// Guard 1: Per-call token cap
		if (estimatedPromptTokens > maxTokensPerCall) {
			throw new BudgetExceededException(
					"Prompt token count (" + estimatedPromptTokens + ") exceeds per-call cap (" + maxTokensPerCall + ")"
			);
		}

		// Guard 2: Monthly dollar budget
		BigDecimal currentMtdCost = usageLedgerPort.getMonthToDateCostUsd(tenantId, YearMonth.now());
		if (currentMtdCost.compareTo(monthlyBudgetUsd) >= 0) {
			throw new BudgetExceededException(
					"Tenant month-to-date cost ($" + currentMtdCost + ") reached monthly budget ($" + monthlyBudgetUsd + ")"
			);
		}
	}

	public int getMaxTokensPerCall() {
		return maxTokensPerCall;
	}

	public BigDecimal getMonthlyBudgetUsd() {
		return monthlyBudgetUsd;
	}
}