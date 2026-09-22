package com.relaya.demo.intake.service;

import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.domain.IntakeStatus;
import com.relaya.demo.intake.domain.ServiceType;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteIntakeServiceTest {

	@Mock
	private IntakeRepositoryPort intakeRepository;

	private DeleteIntakeService deleteIntakeService;

	private final UUID tenantId = UUID.randomUUID();
	private final UUID intakeId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		deleteIntakeService = new DeleteIntakeService(intakeRepository);
	}

	@Test
	@DisplayName("Successfully deletes an intake in INTAKE_SUBMITTED status")
	void shouldSuccessfullyDeleteSubmittedIntake() {
		Intake intake = Intake.createNew(tenantId, "Client A", ServiceType.WEBSITE_DELIVERY);
		when(intakeRepository.findById(tenantId, intakeId)).thenReturn(Optional.of(intake));

		deleteIntakeService.deleteIntake(tenantId, intakeId);

		verify(intakeRepository).delete(tenantId, intakeId);
	}

	@Test
	@DisplayName("Successfully deletes an intake in WRITE_SUCCESS status")
	void shouldSuccessfullyDeleteCompletedIntake() {
		Intake intake = Intake.createNew(tenantId, "Client A", ServiceType.WEBSITE_DELIVERY);
		intake.markAnalysisPending();
		intake.markAnalysisReady();
		intake.markApproved();
		intake.markWritePending();
		intake.markWriteSuccess();

		when(intakeRepository.findById(tenantId, intakeId)).thenReturn(Optional.of(intake));

		deleteIntakeService.deleteIntake(tenantId, intakeId);

		verify(intakeRepository).delete(tenantId, intakeId);
	}

	@Test
	@DisplayName("Throws NoSuchElementException when intake does not exist for tenant")
	void shouldThrowWhenIntakeNotFound() {
		when(intakeRepository.findById(tenantId, intakeId)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> deleteIntakeService.deleteIntake(tenantId, intakeId));
	}

	@Test
	@DisplayName("Rejects deletion when analysis is actively pending")
	void shouldRejectWhenAnalysisPending() {
		Intake intake = Intake.createNew(tenantId, "Client A", ServiceType.WEBSITE_DELIVERY);
		intake.markAnalysisPending();

		when(intakeRepository.findById(tenantId, intakeId)).thenReturn(Optional.of(intake));

		assertThrows(IllegalStateException.class, () -> deleteIntakeService.deleteIntake(tenantId, intakeId));
	}

	@Test
	@DisplayName("Rejects deletion when board write is actively pending")
	void shouldRejectWhenWritePending() {
		Intake intake = Intake.createNew(tenantId, "Client A", ServiceType.WEBSITE_DELIVERY);
		intake.markAnalysisPending();
		intake.markAnalysisReady();
		intake.markApproved();
		intake.markWritePending();

		when(intakeRepository.findById(tenantId, intakeId)).thenReturn(Optional.of(intake));

		assertThrows(IllegalStateException.class, () -> deleteIntakeService.deleteIntake(tenantId, intakeId));
	}
}
