package com.relaya.demo.intake.port.out;

import com.relaya.demo.intake.domain.Intake;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntakeRepositoryPort {

	Intake save(Intake intake);

	Optional<Intake> findById(UUID tenantId, UUID intakeId);

	List<Intake> findAllByTenantId(UUID tenantId);

	boolean existsById(UUID tenantId, UUID intakeId);

	void delete(UUID tenantId, UUID intakeId);
}