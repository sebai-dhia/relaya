package com.relaya.demo.tenant.adapter.out.persistence;

import com.relaya.demo.tenant.port.out.TenantConfigPort;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class TenantConfigJpaAdapter implements TenantConfigPort {

	private final SpringDataTenantConfigRepository repository;

	public TenantConfigJpaAdapter(SpringDataTenantConfigRepository repository) {
		this.repository = Objects.requireNonNull(repository, "repository must not be null");
	}

	@Override
	public Optional<String> findConfigValue(UUID tenantId, String configKey) {
		if (tenantId == null || configKey == null) {
			return Optional.empty();
		}
		return repository.findByTenantIdAndConfigKey(tenantId, configKey)
				.map(TenantConfigJpaEntity::getConfigValue);
	}
}