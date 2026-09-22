package com.relaya.demo.intake.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class IntakeVersion {

	private final UUID id;
	private final UUID tenantId;
	private final UUID intakeId;
	private final int versionNumber;
	private final String rawText;
	private final Instant createdAt;

	public IntakeVersion(UUID id, UUID tenantId, UUID intakeId, int versionNumber,
			String rawText, Instant createdAt) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
		this.intakeId = Objects.requireNonNull(intakeId, "intakeId must not be null");
		if (versionNumber < 1) {
			throw new IllegalArgumentException("versionNumber must be >= 1");
		}
		this.versionNumber = versionNumber;
		this.rawText = Objects.requireNonNull(rawText, "rawText must not be null");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
	}

	public static IntakeVersion createInitial(UUID tenantId, UUID intakeId, String rawText) {
		return new IntakeVersion(UUID.randomUUID(), tenantId, intakeId, 1, rawText, Instant.now());
	}

	public static IntakeVersion createNext(UUID tenantId, UUID intakeId, int nextVersionNumber, String rawText) {
		return new IntakeVersion(UUID.randomUUID(), tenantId, intakeId, nextVersionNumber, rawText, Instant.now());
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

	public int getVersionNumber() {
		return versionNumber;
	}

	public String getRawText() {
		return rawText;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IntakeVersion that)) return false;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}