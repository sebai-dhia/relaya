package com.relaya.demo.intake.adapter.out.persistence;

import com.relaya.demo.intake.domain.IntakeVersion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "intake_versions")
public class IntakeVersionJpaEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "intake_id", nullable = false)
	private UUID intakeId;

	@Column(name = "version_number", nullable = false)
	private int versionNumber;

	@Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")
	private String rawText;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected IntakeVersionJpaEntity() {
	}

	public IntakeVersionJpaEntity(UUID id, UUID tenantId, UUID intakeId,
			int versionNumber, String rawText, Instant createdAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.intakeId = intakeId;
		this.versionNumber = versionNumber;
		this.rawText = rawText;
		this.createdAt = createdAt;
	}

	public static IntakeVersionJpaEntity fromDomain(IntakeVersion domain) {
		return new IntakeVersionJpaEntity(
				domain.getId(),
				domain.getTenantId(),
				domain.getIntakeId(),
				domain.getVersionNumber(),
				domain.getRawText(),
				domain.getCreatedAt()
		);
	}

	public IntakeVersion toDomain() {
		return new IntakeVersion(id, tenantId, intakeId, versionNumber, rawText, createdAt);
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
}