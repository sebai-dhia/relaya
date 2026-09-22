package com.relaya.demo.board.port.out;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.board.domain.IdempotencyKey;
import java.util.Objects;
import java.util.UUID;

public record BoardWriteContext(
		UUID tenantId,
		UUID approvalId,
		String clientLabel,
		AnalysisDraft draft,
		IdempotencyKey idempotencyKey
) {
	public BoardWriteContext {
		Objects.requireNonNull(tenantId, "tenantId must not be null");
		Objects.requireNonNull(approvalId, "approvalId must not be null");
		Objects.requireNonNull(clientLabel, "clientLabel must not be null");
		Objects.requireNonNull(draft, "draft must not be null");
		Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
	}
}