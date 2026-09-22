package com.relaya.demo.analysis.adapter.out.worker;

import com.relaya.demo.analysis.adapter.in.sse.AnalysisSseBroadcaster;
import com.relaya.demo.analysis.adapter.out.ai.PricingCalculator;
import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.port.out.AnalysisDraftRepositoryPort;
import com.relaya.demo.analysis.port.out.ProviderAdapter;
import com.relaya.demo.analysis.port.out.ProviderExtractionResult;
import com.relaya.demo.analysis.port.out.UsageLedgerPort;
import com.relaya.demo.audit.domain.AuditEvent;
import com.relaya.demo.audit.domain.AuditEventType;
import com.relaya.demo.audit.port.out.AuditEventPort;
import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.domain.IntakeVersion;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import com.relaya.demo.intake.port.out.IntakeVersionRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class AsyncAnalysisWorker {

	private static final Logger log = LoggerFactory.getLogger(AsyncAnalysisWorker.class);

	private final IntakeRepositoryPort intakeRepo;
	private final IntakeVersionRepositoryPort versionRepo;
	private final AnalysisDraftRepositoryPort draftRepo;
	private final ProviderAdapter providerAdapter;
	private final UsageLedgerPort usageLedgerPort;
	private final AuditEventPort auditPort;
	private final PricingCalculator pricingCalculator;
	private final AnalysisSseBroadcaster sseBroadcaster;

	public AsyncAnalysisWorker(
			IntakeRepositoryPort intakeRepo,
			IntakeVersionRepositoryPort versionRepo,
			AnalysisDraftRepositoryPort draftRepo,
			ProviderAdapter providerAdapter,
			UsageLedgerPort usageLedgerPort,
			AuditEventPort auditPort,
			PricingCalculator pricingCalculator,
			AnalysisSseBroadcaster sseBroadcaster
	) {
		this.intakeRepo = Objects.requireNonNull(intakeRepo, "intakeRepo must not be null");
		this.versionRepo = Objects.requireNonNull(versionRepo, "versionRepo must not be null");
		this.draftRepo = Objects.requireNonNull(draftRepo, "draftRepo must not be null");
		this.providerAdapter = Objects.requireNonNull(providerAdapter, "providerAdapter must not be null");
		this.usageLedgerPort = Objects.requireNonNull(usageLedgerPort, "usageLedgerPort must not be null");
		this.auditPort = Objects.requireNonNull(auditPort, "auditPort must not be null");
		this.pricingCalculator = Objects.requireNonNull(pricingCalculator, "pricingCalculator must not be null");
		this.sseBroadcaster = Objects.requireNonNull(sseBroadcaster, "sseBroadcaster must not be null");
	}

	@Async
	public void processAnalysis(UUID tenantId, UUID intakeId) {
		log.info("Starting background analysis for intake: {} in tenant: {}", intakeId, tenantId);

		Optional<Intake> intakeOpt = intakeRepo.findById(tenantId, intakeId);
		Optional<IntakeVersion> versionOpt = versionRepo.findLatestByIntakeId(tenantId, intakeId);

		if (intakeOpt.isEmpty() || versionOpt.isEmpty()) {
			log.error("Intake or version not found for async analysis: intakeId={}", intakeId);
			sseBroadcaster.broadcastFailed(intakeId, "Intake or version not found");
			return;
		}

		Intake intake = intakeOpt.get();
		IntakeVersion version = versionOpt.get();

		try {
			intake.markAnalysisPending();
			intakeRepo.save(intake);
			auditPort.recordEvent(AuditEvent.create(tenantId, intakeId, null, AuditEventType.ANALYSIS_STARTED, null));
			sseBroadcaster.broadcastStarted(intakeId);

			ProviderExtractionResult result = providerAdapter.extractAnalysis(tenantId, version.getRawText());

			BigDecimal cost = pricingCalculator.calculateCost(result.model(), result.promptTokens(), result.completionTokens());
			usageLedgerPort.recordUsage(
					tenantId, intakeId, result.provider(), result.model(),
					result.promptTokens(), result.completionTokens(), cost
			);

			AnalysisDraft draft = AnalysisDraft.createNew(
					tenantId, intakeId, version.getId(),
					result.objectives(), result.deliverables(), result.constraints(),
					result.timelineNotes(), result.budgetNotes(),
					result.unknowns(), result.proposedTasks()
			);
			draftRepo.save(draft);

			intake.markAnalysisReady();
			intakeRepo.save(intake);
			auditPort.recordEvent(AuditEvent.create(tenantId, intakeId, null, AuditEventType.ANALYSIS_COMPLETED, null));
			sseBroadcaster.broadcastCompleted(intakeId, draft.getId());

			log.info("Async analysis successfully completed for intake: {}, draftId: {}", intakeId, draft.getId());
		} catch (Exception e) {
			log.error("Async analysis failed for intake {}: {}", intakeId, e.getMessage(), e);
			try {
				intake.markAnalysisFailed();
				intakeRepo.save(intake);
				auditPort.recordEvent(AuditEvent.create(tenantId, intakeId, null, AuditEventType.ANALYSIS_FAILED, e.getMessage()));
			} catch (Exception ex) {
				log.error("Failed to mark intake as failed: {}", ex.getMessage());
			}
			sseBroadcaster.broadcastFailed(intakeId, e.getMessage());
		}
	}
}