package com.relaya.demo.board.domain;

import com.relaya.demo.analysis.domain.ContentHash;
import java.util.Objects;
import java.util.UUID;

public record IdempotencyKey(String value) {

	public IdempotencyKey {
		Objects.requireNonNull(value, "value must not be null");
		if (value.isBlank()) {
			throw new IllegalArgumentException("IdempotencyKey value must not be blank");
		}
	}

	public static IdempotencyKey of(UUID approvalId, ContentHash contentHashAtApproval) {
		Objects.requireNonNull(approvalId, "approvalId must not be null");
		Objects.requireNonNull(contentHashAtApproval, "contentHashAtApproval must not be null");
		return new IdempotencyKey(approvalId + "_" + contentHashAtApproval.value());
	}
}