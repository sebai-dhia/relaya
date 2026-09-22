package com.relaya.demo.tenant.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_config")
public class TenantConfigJpaEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "config_key", nullable = false, length = 100)
	private String configKey;

	@Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
	private String configValue;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected TenantConfigJpaEntity() {
	}

	public TenantConfigJpaEntity(UUID id, UUID tenantId, String configKey, String configValue, Instant updatedAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.configKey = configKey;
		this.configValue = configValue;
		this.updatedAt = updatedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public String getConfigKey() {
		return configKey;
	}

	public String getConfigValue() {
		return configValue;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}