package com.relaya.demo.tenant.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTenantConfigRepository extends JpaRepository<TenantConfigJpaEntity, UUID> {

	Optional<TenantConfigJpaEntity> findByTenantIdAndConfigKey(UUID tenantId, String configKey);
}