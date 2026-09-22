package com.relaya.demo.review.adapter.out.persistence;

import com.relaya.demo.analysis.domain.ContentHash;
import com.relaya.demo.review.domain.Approval;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approvals")
public class ApprovalJpaEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "draft_id", nullable = false)
	private UUID draftId;

	@Column(name = "reviewer_user_id", nullable = false)
	private UUID reviewerUserId;

	@Column(name = "content_hash_at_approval", nullable = false, length = 64)
	private String contentHashAtApproval;

	@Column(name = "approved_at", nullable = false, updatable = false)
	private Instant approvedAt;

	@Column(name = "invalidated_at")
	private Instant invalidatedAt;

	protected ApprovalJpaEntity() {
	}

	public ApprovalJpaEntity(UUID id, UUID tenantId, UUID draftId, UUID reviewerUserId,
			String contentHashAtApproval, Instant approvedAt, Instant invalidatedAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.draftId = draftId;
		this.reviewerUserId = reviewerUserId;
		this.contentHashAtApproval = contentHashAtApproval;
		this.approvedAt = approvedAt;
		this.invalidatedAt = invalidatedAt;
	}

	public static ApprovalJpaEntity fromDomain(Approval domain) {
		return new ApprovalJpaEntity(
				domain.getId(),
				domain.getTenantId(),
				domain.getDraftId(),
				domain.getReviewerUserId(),
				domain.getContentHashAtApproval().value(),
				domain.getApprovedAt(),
				domain.getInvalidatedAt()
		);
	}

	public Approval toDomain() {
		return new Approval(
				id, tenantId, draftId, reviewerUserId,
				new ContentHash(contentHashAtApproval),
				approvedAt, invalidatedAt
		);
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

	public String getContentHashAtApproval() {
		return contentHashAtApproval;
	}

	public Instant getApprovedAt() {
		return approvedAt;
	}

	public Instant getInvalidatedAt() {
		return invalidatedAt;
	}
}