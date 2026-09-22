package com.relaya.demo.intake.port.in;

import java.util.UUID;

/**
 * Inbound port for deleting an intake brief and its associated records.
 */
public interface DeleteIntakeUseCase {

	void deleteIntake(UUID tenantId, UUID intakeId);
}
