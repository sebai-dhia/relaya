package com.relaya.demo.board.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class BoardWrite {

	private final UUID id;
	private final UUID tenantId;
	private final UUID approvalId;
	private final IdempotencyKey idempotencyKey;
	private final String destinationBoard;
	private String trelloCardId;
	private String trelloCardUrl;
	private WriteStatus status;
	private String errorDetails;
	private final Instant createdAt;
	private Instant completedAt;

	public BoardWrite(
			UUID id,
			UUID tenantId,
			UUID approvalId,
			IdempotencyKey idempotencyKey,
			String destinationBoard,
			String trelloCardId,
			String trelloCardUrl,
			WriteStatus status,
			String errorDetails,
			Instant createdAt,
			Instant completedAt
	) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
		this.approvalId = Objects.requireNonNull(approvalId, "approvalId must not be null");
		this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
		this.destinationBoard = (destinationBoard == null || destinationBoard.isBlank()) ? "TRELLO" : destinationBoard;
		this.trelloCardId = trelloCardId;
		this.trelloCardUrl = trelloCardUrl;
		this.status = Objects.requireNonNull(status, "status must not be null");
		this.errorDetails = errorDetails;
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
		this.completedAt = completedAt;
	}

	public static BoardWrite createPending(UUID tenantId, UUID approvalId, IdempotencyKey key) {
		return new BoardWrite(
				UUID.randomUUID(), tenantId, approvalId, key,
				"TRELLO", null, null, WriteStatus.PENDING, null, Instant.now(), null
		);
	}

	public void markSuccess(String cardId, String cardUrl) {
		this.trelloCardId = cardId;
		this.trelloCardUrl = cardUrl;
		this.status = WriteStatus.SUCCESS;
		this.errorDetails = null;
		this.completedAt = Instant.now();
	}

	public void markFailed(String error) {
		this.status = WriteStatus.FAILED;
		this.errorDetails = (error == null || error.isBlank()) ? "Unknown error" : error;
		this.completedAt = Instant.now();
	}

	public void markStub(String stubCardId, String stubCardUrl) {
		this.trelloCardId = stubCardId;
		this.trelloCardUrl = stubCardUrl;
		this.status = WriteStatus.STUB;
		this.errorDetails = null;
		this.completedAt = Instant.now();
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

	public IdempotencyKey getIdempotencyKey() {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BoardWrite that)) return false;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}