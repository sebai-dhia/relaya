package com.relaya.demo.intake.service;

import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.domain.IntakeStatus;
import com.relaya.demo.intake.port.in.DeleteIntakeUseCase;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
public class DeleteIntakeService implements DeleteIntakeUseCase {

	private final IntakeRepositoryPort intakeRepository;

	public DeleteIntakeService(IntakeRepositoryPort intakeRepository) {
		this.intakeRepository = Objects.requireNonNull(intakeRepository, "intakeRepository must not be null");
	}

	@Override
	@Transactional
	public void deleteIntake(UUID tenantId, UUID intakeId) {
		Objects.requireNonNull(tenantId, "tenantId must not be null");
		Objects.requireNonNull(intakeId, "intakeId must not be null");

		Intake intake = intakeRepository.findById(tenantId, intakeId)
				.orElseThrow(() -> new NoSuchElementException("Intake not found: " + intakeId));

		if (intake.getStatus() == IntakeStatus.ANALYSIS_PENDING || intake.getStatus() == IntakeStatus.WRITE_PENDING) {
			throw new IllegalStateException("Cannot delete intake while an operation is pending: " + intake.getStatus());
		}

		intakeRepository.delete(tenantId, intakeId);
	}
}
