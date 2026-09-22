package com.relaya.demo.intake.service;

import com.relaya.demo.audit.domain.AuditEvent;
import com.relaya.demo.audit.domain.AuditEventType;
import com.relaya.demo.audit.port.out.AuditEventPort;
import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.domain.IntakeVersion;
import com.relaya.demo.intake.port.in.SubmitIntakeCommand;
import com.relaya.demo.intake.port.in.SubmitIntakeUseCase;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import com.relaya.demo.intake.port.out.IntakeVersionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class SubmitIntakeService implements SubmitIntakeUseCase {

	private final IntakeRepositoryPort intakeRepo;
	private final IntakeVersionRepositoryPort versionRepo;
	private final AuditEventPort auditPort;

	public SubmitIntakeService(
			IntakeRepositoryPort intakeRepo,
			IntakeVersionRepositoryPort versionRepo,
			AuditEventPort auditPort
	) {
		this.intakeRepo = Objects.requireNonNull(intakeRepo, "intakeRepo must not be null");
		this.versionRepo = Objects.requireNonNull(versionRepo, "versionRepo must not be null");
		this.auditPort = Objects.requireNonNull(auditPort, "auditPort must not be null");
	}

	@Override
	@Transactional
	public Intake submitIntake(SubmitIntakeCommand command) {
		Intake intake = Intake.createNew(command.tenantId(), command.clientLabel(), command.serviceType());
		Intake savedIntake = intakeRepo.save(intake);

		IntakeVersion version = IntakeVersion.createInitial(command.tenantId(), savedIntake.getId(), command.rawText());
		versionRepo.save(version);

		AuditEvent auditEvent = AuditEvent.create(
				command.tenantId(),
				savedIntake.getId(),
				null,
				AuditEventType.INTAKE_SUBMITTED,
				null
		);
		auditPort.recordEvent(auditEvent);

		return savedIntake;
	}
}