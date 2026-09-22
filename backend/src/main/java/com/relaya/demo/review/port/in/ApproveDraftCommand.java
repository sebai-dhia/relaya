package com.relaya.demo.review.port.in;

import com.relaya.demo.analysis.domain.ContentHash;
import java.util.Objects;
import java.util.UUID;

public record ApproveDraftCommand(
		UUID tenantId,
		UUID draftId,
		UUID reviewerUserId,
		ContentHash expectedContentHash
) {
	public ApproveDraftCommand {
		Objects.requireNonNull(tenantId, "tenantId must not be null");
		Objects.requireNonNull(draftId, "draftId must not be null");
		Objects.requireNonNull(reviewerUserId, "reviewerUserId must not be null");
		Objects.requireNonNull(expectedContentHash, "expectedContentHash must not be null");
	}
}