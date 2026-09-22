package com.relaya.demo.intake.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntakeStateMachineTest {

	@Test
	@DisplayName("Happy path lifecycle progresses correctly through states")
	void shouldFollowHappyPathStateProgression() {
		Intake intake = Intake.createNew(UUID.randomUUID(), "Acme Corp", ServiceType.WEBSITE_DELIVERY);
		assertEquals(IntakeStatus.INTAKE_SUBMITTED, intake.getStatus());

		intake.markAnalysisPending();
		assertEquals(IntakeStatus.ANALYSIS_PENDING, intake.getStatus());

		intake.markAnalysisReady();
		assertEquals(IntakeStatus.ANALYSIS_READY, intake.getStatus());

		intake.markApproved();
		assertEquals(IntakeStatus.APPROVED, intake.getStatus());

		intake.markWritePending();
		assertEquals(IntakeStatus.WRITE_PENDING, intake.getStatus());

		intake.markWriteSuccess();
		assertEquals(IntakeStatus.WRITE_SUCCESS, intake.getStatus());
	}

	@Test
	@DisplayName("Cannot skip analysis and approve directly from INTAKE_SUBMITTED")
	void shouldRejectDirectApprovalWithoutAnalysis() {
		Intake intake = Intake.createNew(UUID.randomUUID(), "Acme Corp", ServiceType.WEBSITE_DELIVERY);
		assertThrows(IllegalStateException.class, intake::markApproved);
	}

	@Test
	@DisplayName("Cannot write to board without approval")
	void shouldRejectBoardWriteWithoutApproval() {
		Intake intake = Intake.createNew(UUID.randomUUID(), "Acme Corp", ServiceType.WEBSITE_DELIVERY);
		intake.markAnalysisPending();
		intake.markAnalysisReady();

		assertThrows(IllegalStateException.class, intake::markWritePending);
	}

	@Test
	@DisplayName("Re-analysis allowed from ANALYSIS_FAILED and DRAFT_EDITED")
	void shouldAllowReanalysisFromFailedOrEdited() {
		Intake intake = Intake.createNew(UUID.randomUUID(), "Acme Corp", ServiceType.WEBSITE_DELIVERY);
		intake.markAnalysisPending();
		intake.markAnalysisFailed();
		assertEquals(IntakeStatus.ANALYSIS_FAILED, intake.getStatus());

		intake.markAnalysisPending();
		assertEquals(IntakeStatus.ANALYSIS_PENDING, intake.getStatus());

		intake.markAnalysisReady();
		intake.markDraftEdited();
		assertEquals(IntakeStatus.DRAFT_EDITED, intake.getStatus());

		intake.markAnalysisPending();
		assertEquals(IntakeStatus.ANALYSIS_PENDING, intake.getStatus());
	}
}