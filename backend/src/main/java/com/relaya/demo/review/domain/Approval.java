package com.relaya.demo.review.domain;

import com.relaya.demo.analysis.domain.ContentHash;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Approval {

	private final UUID id;
	private final UUID tenantId;
	private final UUID draftId;
	private final UUID reviewerUserId;
	private final ContentHash contentHashAtApproval;
	private final Instant approvedAt;
	private Instant invalidatedAt;

	public Approval(
			UUID id,
			UUID tenantId,
			UUID draftId,
			UUID reviewerUserId,
			ContentHash contentHashAtApproval,
			Instant approvedAt,
			Instant invalidatedAt
	) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
		this.draftId = Objects.requireNonNull(draftId, "draftId must not be null");
		this.reviewerUserId = Objects.requireNonNull(reviewerUserId, "reviewerUserId must not be null");
		this.contentHashAtApproval = Objects.requireNonNull(contentHashAtApproval, "contentHashAtApproval must not be null");
		this.approvedAt = Objects.requireNonNull(approvedAt, "approvedAt must not be null");
		this.invalidatedAt = invalidatedAt;
	}

	public static Approval createNew(UUID tenantId, UUID draftId, UUID reviewerUserId, ContentHash hash) {
		return new Approval(UUID.randomUUID(), tenantId, draftId, reviewerUserId, hash, Instant.now(), null);
	}

	public boolean isValid() {
		return this.invalidatedAt == null;
	}

	public boolean matchesCurrentDraftHash(ContentHash currentHash) {
		if (currentHash == null) {
			return false;
		}
		return this.contentHashAtApproval.equals(currentHash);
	}

	public void invalidate() {
		if (this.invalidatedAt == null) {
			this.invalidatedAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public UUID getDraftId() {
		return draftId;
	}

	public UUID getReviewerUserId() {
		return reviewerUserId;
	}

	public ContentHash getContentHashAtApproval() {
		return contentHashAtApproval;
	}

	public Instant getApprovedAt() {
		return approvedAt;
	}

	public Instant getInvalidatedAt() {
		return invalidatedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Approval that)) return false;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}