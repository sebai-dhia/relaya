package com.relaya.demo.intake.adapter.out.persistence;

import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.domain.IntakeStatus;
import com.relaya.demo.intake.domain.ServiceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "intakes")
public class IntakeJpaEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "client_label", nullable = false, length = 150)
	private String clientLabel;

	@Enumerated(EnumType.STRING)
	@Column(name = "service_type", nullable = false, length = 50)
	private ServiceType serviceType;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private IntakeStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected IntakeJpaEntity() {
	}

	public IntakeJpaEntity(UUID id, UUID tenantId, String clientLabel,
			ServiceType serviceType, IntakeStatus status, Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.clientLabel = clientLabel;
		this.serviceType = serviceType;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static IntakeJpaEntity fromDomain(Intake domain) {
		return new IntakeJpaEntity(
				domain.getId(),
				domain.getTenantId(),
				domain.getClientLabel(),
				domain.getServiceType(),
				domain.getStatus(),
				domain.getCreatedAt(),
				domain.getUpdatedAt()
		);
	}

	public Intake toDomain() {
		return new Intake(id, tenantId, clientLabel, serviceType, status, createdAt, updatedAt);
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
}