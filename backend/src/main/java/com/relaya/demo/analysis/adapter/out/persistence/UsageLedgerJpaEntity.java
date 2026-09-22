package com.relaya.demo.analysis.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usage_ledger")
public class UsageLedgerJpaEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "intake_id")
	private UUID intakeId;

	@Column(name = "provider", nullable = false, length = 50)
	private String provider;

	@Column(name = "model", nullable = false, length = 100)
	private String model;

	@Column(name = "prompt_tokens", nullable = false)
	private int promptTokens;

	@Column(name = "completion_tokens", nullable = false)
	private int completionTokens;

	@Column(name = "estimated_cost_usd", nullable = false, precision = 10, scale = 6)
	private BigDecimal estimatedCostUsd;

	@Column(name = "called_at", nullable = false, updatable = false)
	private Instant calledAt;

	protected UsageLedgerJpaEntity() {
	}

	public UsageLedgerJpaEntity(UUID id, UUID tenantId, UUID intakeId,
			String provider, String model, int promptTokens, int completionTokens,
			BigDecimal estimatedCostUsd, Instant calledAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.intakeId = intakeId;
		this.provider = provider;
		this.model = model;
		this.promptTokens = promptTokens;
		this.completionTokens = completionTokens;
		this.estimatedCostUsd = estimatedCostUsd;
		this.calledAt = calledAt;
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

	public String getProvider() {
		return provider;
	}

	public String getModel() {
		return model;
	}

	public int getPromptTokens() {
		return promptTokens;
	}

	public int getCompletionTokens() {
		return completionTokens;
	}

	public BigDecimal getEstimatedCostUsd() {
		return estimatedCostUsd;
	}

	public Instant getCalledAt() {
		return calledAt;
	}
}