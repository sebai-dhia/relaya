package com.relaya.demo.board.adapter.out.persistence;

import com.relaya.demo.board.domain.BoardWrite;
import com.relaya.demo.board.domain.IdempotencyKey;
import com.relaya.demo.board.domain.WriteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "board_writes")
public class BoardWriteJpaEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "approval_id", nullable = false, unique = true)
	private UUID approvalId;

	@Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
	private String idempotencyKey;

	@Column(name = "destination_board", nullable = false, length = 100)
	private String destinationBoard;

	@Column(name = "trello_card_id", length = 100)
	private String trelloCardId;

	@Column(name = "trello_card_url", columnDefinition = "TEXT")
	private String trelloCardUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private WriteStatus status;

	@Column(name = "error_details", columnDefinition = "TEXT")
	private String errorDetails;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	protected BoardWriteJpaEntity() {
	}

	public BoardWriteJpaEntity(
			UUID id, UUID tenantId, UUID approvalId, String idempotencyKey,
			String destinationBoard, String trelloCardId, String trelloCardUrl,
			WriteStatus status, String errorDetails, Instant createdAt, Instant completedAt
	) {
		this.id = id;
		this.tenantId = tenantId;
		this.approvalId = approvalId;
		this.idempotencyKey = idempotencyKey;
		this.destinationBoard = destinationBoard;
		this.trelloCardId = trelloCardId;
		this.trelloCardUrl = trelloCardUrl;
		this.status = status;
		this.errorDetails = errorDetails;
		this.createdAt = createdAt;
		this.completedAt = completedAt;
	}

	public static BoardWriteJpaEntity fromDomain(BoardWrite domain) {
		return new BoardWriteJpaEntity(
				domain.getId(),
				domain.getTenantId(),
				domain.getApprovalId(),
				domain.getIdempotencyKey().value(),
				domain.getDestinationBoard(),
				domain.getTrelloCardId(),
				domain.getTrelloCardUrl(),
				domain.getStatus(),
				domain.getErrorDetails(),
				domain.getCreatedAt(),
				domain.getCompletedAt()
		);
	}

	public BoardWrite toDomain() {
		return new BoardWrite(
				id, tenantId, approvalId, new IdempotencyKey(idempotencyKey),
				destinationBoard, trelloCardId, trelloCardUrl, status,
				errorDetails, createdAt, completedAt
		);
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public UUID getApprovalId() {
		return approvalId;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public String getDestinationBoard() {
		return destinationBoard;
	}

	public String getTrelloCardId() {
		return trelloCardId;
	}

	public String getTrelloCardUrl() {
		return trelloCardUrl;
	}

	public WriteStatus getStatus() {
		return status;
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}
}