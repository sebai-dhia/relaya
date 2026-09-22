package com.relaya.demo.audit.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AuditEvent(
		UUID id,
		UUID tenantId,
		UUID intakeId,
		UUID userId,
		AuditEventType eventType,
		String payloadJson,
		Instant occurredAt
) {
	public AuditEvent {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(tenantId, "tenantId must not be null");
		Objects.requireNonNull(eventType, "eventType must not be null");
		Objects.requireNonNull(occurredAt, "occurredAt must not be null");
	}

	public static AuditEvent create(
			UUID tenantId,
			UUID intakeId,
			UUID userId,
			AuditEventType eventType,
			String payloadJson
	) {
		return new AuditEvent(
				UUID.randomUUID(),
				tenantId,
				intakeId,
				userId,
				eventType,
				payloadJson,
				Instant.now()
		);
	}
}