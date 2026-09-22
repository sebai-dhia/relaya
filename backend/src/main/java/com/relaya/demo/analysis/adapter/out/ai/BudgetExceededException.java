package com.relaya.demo.analysis.adapter.out.ai;

public class BudgetExceededException extends RuntimeException {

	public BudgetExceededException(String message) {
		super(message);
	}
}