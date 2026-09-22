package com.relaya.demo.intake.port.in;

import com.relaya.demo.intake.domain.Intake;

public interface SubmitIntakeUseCase {

	Intake submitIntake(SubmitIntakeCommand command);
}