package com.relaya.demo.board.port.in;

import com.relaya.demo.board.domain.IdempotencyKey;

import java.util.Objects;
import java.util.UUID;

public record TriggerBoardWriteCommand(
		UUID tenantId,
		UUID approvalId,
		IdempotencyKey idempotencyKey
) {

	public TriggerBoardWriteCommand(UUID tenantId, UUID approvalId) {
		this(tenantId, approvalId, (IdempotencyKey) null);
	}

	public TriggerBoardWriteCommand {
		Objects.requireNonNull(tenantId, "tenantId must not be null");
		Objects.requireNonNull(approvalId, "approvalId must not be null");
		if (idempotencyKey == null) {
			idempotencyKey = new IdempotencyKey(UUID.randomUUID().toString());
		}
	}
}