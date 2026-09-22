package com.relaya.demo.audit.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpringDataAuditEventRepository extends JpaRepository<AuditEventJpaEntity, UUID> {

	List<AuditEventJpaEntity> findAllByTenantIdAndIntakeIdOrderByOccurredAtDesc(UUID tenantId, UUID intakeId);

	List<AuditEventJpaEntity> findAllByTenantIdOrderByOccurredAtDesc(UUID tenantId);
}