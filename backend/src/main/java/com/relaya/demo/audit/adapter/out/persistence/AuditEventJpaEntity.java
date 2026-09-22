package com.relaya.demo.audit.adapter.out.persistence;

import com.relaya.demo.audit.domain.AuditEvent;
import com.relaya.demo.audit.domain.AuditEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
public class AuditEventJpaEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "intake_id")
	private UUID intakeId;

	@Column(name = "user_id")
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 100)
	private AuditEventType eventType;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "payload", columnDefinition = "jsonb")
	private String payload;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	private Instant occurredAt;

	protected AuditEventJpaEntity() {
	}

	public AuditEventJpaEntity(UUID id, UUID tenantId, UUID intakeId, UUID userId,
			AuditEventType eventType, String payload, Instant occurredAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.intakeId = intakeId;
		this.userId = userId;
		this.eventType = eventType;
		this.payload = payload;
		this.occurredAt = occurredAt;
	}

	public static AuditEventJpaEntity fromDomain(AuditEvent domain) {
		return new AuditEventJpaEntity(
				domain.id(),
				domain.tenantId(),
				domain.intakeId(),
				domain.userId(),
				domain.eventType(),
				domain.payloadJson(),
				domain.occurredAt()
		);
	}

	public AuditEvent toDomain() {
		return new AuditEvent(id, tenantId, intakeId, userId, eventType, payload, occurredAt);
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public UUID getIntakeId() {
		return intakeId;
	}

	public UUID getUserId() {
		return userId;
	}

	public AuditEventType getEventType() {
		return eventType;
	}

	public String getPayload() {
		return payload;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}
}