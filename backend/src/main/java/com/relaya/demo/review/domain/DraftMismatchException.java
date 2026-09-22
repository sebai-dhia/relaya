package com.relaya.demo.review.domain;

public class DraftMismatchException extends RuntimeException {

	public DraftMismatchException(String message) {
		super(message);
	}
}