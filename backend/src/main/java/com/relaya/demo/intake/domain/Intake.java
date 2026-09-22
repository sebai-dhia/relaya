package com.relaya.demo.intake.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Intake {

	private final UUID id;
	private final UUID tenantId;
	private String clientLabel;
	private final ServiceType serviceType;
	private IntakeStatus status;
	private final Instant createdAt;
	private Instant updatedAt;

	public Intake(UUID id, UUID tenantId, String clientLabel, ServiceType serviceType,
			IntakeStatus status, Instant createdAt, Instant updatedAt) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
		this.clientLabel = Objects.requireNonNull(clientLabel, "clientLabel must not be null");
		this.serviceType = Objects.requireNonNull(serviceType, "serviceType must not be null");
		this.status = Objects.requireNonNull(status, "status must not be null");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
		this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
	}

	public static Intake createNew(UUID tenantId, String clientLabel, ServiceType serviceType) {
		Instant now = Instant.now();
		return new Intake(UUID.randomUUID(), tenantId, clientLabel, serviceType,
				IntakeStatus.INTAKE_SUBMITTED, now, now);
	}

	public void updateClientLabel(String newLabel) {
		if (newLabel == null || newLabel.isBlank()) {
			throw new IllegalArgumentException("Client label must not be empty");
		}
		this.clientLabel = newLabel.trim();
		this.updatedAt = Instant.now();
	}

	public void markAnalysisPending() {
		if (status != IntakeStatus.INTAKE_SUBMITTED
				&& status != IntakeStatus.ANALYSIS_FAILED
				&& status != IntakeStatus.DRAFT_EDITED
				&& status != IntakeStatus.ANALYSIS_READY) {
			throw new IllegalStateException("Cannot start analysis from state: " + status);
		}
		transitionTo(IntakeStatus.ANALYSIS_PENDING);
	}

	public void markAnalysisReady() {
		if (status != IntakeStatus.ANALYSIS_PENDING) {
			throw new IllegalStateException("Cannot mark ready from state: " + status);
		}
		transitionTo(IntakeStatus.ANALYSIS_READY);
	}

	public void markAnalysisFailed() {
		if (status != IntakeStatus.ANALYSIS_PENDING) {
			throw new IllegalStateException("Cannot mark failed from state: " + status);
		}
		transitionTo(IntakeStatus.ANALYSIS_FAILED);
	}

	public void markDraftEdited() {
		if (status != IntakeStatus.ANALYSIS_READY
				&& status != IntakeStatus.DRAFT_EDITED
				&& status != IntakeStatus.APPROVED
				&& status != IntakeStatus.INVALIDATED) {
			throw new IllegalStateException("Cannot edit draft from state: " + status);
		}
		transitionTo(IntakeStatus.DRAFT_EDITED);
	}

	public void markApproved() {
		if (status != IntakeStatus.ANALYSIS_READY && status != IntakeStatus.DRAFT_EDITED) {
			throw new IllegalStateException("Cannot approve from state: " + status);
		}
		transitionTo(IntakeStatus.APPROVED);
	}

	public void markWritePending() {
		if (status != IntakeStatus.APPROVED) {
			throw new IllegalStateException("Cannot initiate board write from state: " + status);
		}
		transitionTo(IntakeStatus.WRITE_PENDING);
	}

	public void markWriteSuccess() {
		if (status != IntakeStatus.WRITE_PENDING) {
			throw new IllegalStateException("Cannot mark write success from state: " + status);
		}
		transitionTo(IntakeStatus.WRITE_SUCCESS);
	}

	public void markWriteFailed() {
		if (status != IntakeStatus.WRITE_PENDING) {
			throw new IllegalStateException("Cannot mark write failed from state: " + status);
		}
		transitionTo(IntakeStatus.WRITE_FAILED);
	}

	public void markInvalidated() {
		if (status != IntakeStatus.APPROVED && status != IntakeStatus.WRITE_PENDING) {
			throw new IllegalStateException("Cannot invalidate approval from state: " + status);
		}
		transitionTo(IntakeStatus.INVALIDATED);
	}

	private void transitionTo(IntakeStatus newStatus) {
		this.status = newStatus;
		this.updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public String getClientLabel() {
		return clientLabel;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public IntakeStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Intake that)) return false;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}