package com.relaya.demo.analysis.adapter.out.ai;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PricingCalculator {

	private static final BigDecimal MILLION = new BigDecimal("1000000");

	// Rates per 1M tokens in USD
	// llama-3.3-70b-versatile: $0.59 / 1M prompt, $0.79 / 1M completion
	private static final BigDecimal DEFAULT_PROMPT_RATE = new BigDecimal("0.59");
	private static final BigDecimal DEFAULT_COMPLETION_RATE = new BigDecimal("0.79");

	public BigDecimal calculateCost(String model, int promptTokens, int completionTokens) {
		if (promptTokens <= 0 && completionTokens <= 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal promptRate = DEFAULT_PROMPT_RATE;
		BigDecimal completionRate = DEFAULT_COMPLETION_RATE;

		BigDecimal promptCost = promptRate.multiply(BigDecimal.valueOf(promptTokens)).divide(MILLION, 6, RoundingMode.HALF_UP);
		BigDecimal completionCost = completionRate.multiply(BigDecimal.valueOf(completionTokens)).divide(MILLION, 6, RoundingMode.HALF_UP);

		return promptCost.add(completionCost);
	}
}