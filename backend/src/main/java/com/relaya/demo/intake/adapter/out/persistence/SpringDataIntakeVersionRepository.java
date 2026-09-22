package com.relaya.demo.intake.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataIntakeVersionRepository extends JpaRepository<IntakeVersionJpaEntity, UUID> {

	Optional<IntakeVersionJpaEntity> findFirstByTenantIdAndIntakeIdOrderByVersionNumberDesc(UUID tenantId, UUID intakeId);

	Optional<IntakeVersionJpaEntity> findByTenantIdAndIntakeIdAndVersionNumber(UUID tenantId, UUID intakeId, int versionNumber);

	List<IntakeVersionJpaEntity> findAllByTenantIdAndIntakeIdOrderByVersionNumberAsc(UUID tenantId, UUID intakeId);
}