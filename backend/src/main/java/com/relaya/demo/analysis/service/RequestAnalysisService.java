package com.relaya.demo.analysis.service;

import com.relaya.demo.analysis.adapter.out.ai.BudgetGuard;
import com.relaya.demo.analysis.adapter.out.worker.AsyncAnalysisWorker;
import com.relaya.demo.analysis.port.in.RequestAnalysisCommand;
import com.relaya.demo.analysis.port.in.RequestAnalysisUseCase;
import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.domain.IntakeStatus;
import com.relaya.demo.intake.domain.IntakeVersion;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import com.relaya.demo.intake.port.out.IntakeVersionRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
public class RequestAnalysisService implements RequestAnalysisUseCase {

	private final IntakeRepositoryPort intakeRepo;
	private final IntakeVersionRepositoryPort versionRepo;
	private final BudgetGuard budgetGuard;
	private final AsyncAnalysisWorker asyncAnalysisWorker;

	public RequestAnalysisService(
			IntakeRepositoryPort intakeRepo,
			IntakeVersionRepositoryPort versionRepo,
			BudgetGuard budgetGuard,
			AsyncAnalysisWorker asyncAnalysisWorker
	) {
		this.intakeRepo = Objects.requireNonNull(intakeRepo, "intakeRepo must not be null");
		this.versionRepo = Objects.requireNonNull(versionRepo, "versionRepo must not be null");
		this.budgetGuard = Objects.requireNonNull(budgetGuard, "budgetGuard must not be null");
		this.asyncAnalysisWorker = Objects.requireNonNull(asyncAnalysisWorker, "asyncAnalysisWorker must not be null");
	}

	@Override
	public void requestAnalysis(RequestAnalysisCommand command) {
		UUID tenantId = command.tenantId();
		UUID intakeId = command.intakeId();

		Intake intake = intakeRepo.findById(tenantId, intakeId)
				.orElseThrow(() -> new NoSuchElementException("Intake not found: " + intakeId));

		if (intake.getStatus() == IntakeStatus.ANALYSIS_PENDING) {
			throw new IllegalStateException("Analysis already in progress for intake: " + intakeId);
		}

		IntakeVersion version = versionRepo.findLatestByIntakeId(tenantId, intakeId)
				.orElseThrow(() -> new NoSuchElementException("No version found for intake: " + intakeId));

		int estimatedTokens = Math.max(1, version.getRawText().length() / 4);
		budgetGuard.checkGuards(tenantId, estimatedTokens);

		asyncAnalysisWorker.processAnalysis(tenantId, intakeId);
	}
}