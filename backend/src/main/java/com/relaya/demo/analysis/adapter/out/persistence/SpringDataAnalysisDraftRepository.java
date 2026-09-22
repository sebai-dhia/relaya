package com.relaya.demo.analysis.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataAnalysisDraftRepository extends JpaRepository<AnalysisDraftJpaEntity, UUID> {

	Optional<AnalysisDraftJpaEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<AnalysisDraftJpaEntity> findByTenantIdAndIntakeId(UUID tenantId, UUID intakeId);

	Optional<AnalysisDraftJpaEntity> findByTenantIdAndIntakeVersionId(UUID tenantId, UUID intakeVersionId);
}