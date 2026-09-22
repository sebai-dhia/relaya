package com.relaya.demo.analysis.adapter.out.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingCalculatorTest {

	private final PricingCalculator calculator = new PricingCalculator();

	@Test
	@DisplayName("Returns zero when token counts are zero")
	void shouldReturnZeroForZeroTokens() {
		BigDecimal cost = calculator.calculateCost("llama-3.3-70b-versatile", 0, 0);
		assertEquals(BigDecimal.ZERO, cost);
	}

	@Test
	@DisplayName("Correctly calculates cost for 1,000 prompt tokens and 500 completion tokens")
	void shouldCalculateAccurateCost() {
		// Prompt: 1000 * 0.59 / 1M = 0.000590
		// Completion: 500 * 0.79 / 1M = 0.000395
		// Total: 0.000985
		BigDecimal cost = calculator.calculateCost("llama-3.3-70b-versatile", 1000, 500);
		assertEquals(new BigDecimal("0.000985"), cost);
	}
}