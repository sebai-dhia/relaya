package com.relaya.demo.intake.port.in;

import com.relaya.demo.intake.domain.Intake;
import java.util.Optional;
import java.util.UUID;

public interface GetIntakeQuery {

	Optional<Intake> getIntake(UUID tenantId, UUID intakeId);
}