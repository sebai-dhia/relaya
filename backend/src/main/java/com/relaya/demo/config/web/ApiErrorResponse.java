package com.relaya.demo.config.web;

import java.time.Instant;

public record ApiErrorResponse(
		String error,
		String message,
		Instant timestamp
) {

	public static ApiErrorResponse of(String error, String message) {
		return new ApiErrorResponse(error, message, Instant.now());
	}
}