package com.relaya.demo.intake.port.out;

import com.relaya.demo.intake.domain.IntakeVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntakeVersionRepositoryPort {

	IntakeVersion save(IntakeVersion version);

	Optional<IntakeVersion> findLatestByIntakeId(UUID tenantId, UUID intakeId);

	Optional<IntakeVersion> findByIntakeIdAndVersion(UUID tenantId, UUID intakeId, int versionNumber);

	List<IntakeVersion> findAllByIntakeId(UUID tenantId, UUID intakeId);
}