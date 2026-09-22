package com.relaya.demo;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.domain.SourceExcerpt;
import com.relaya.demo.analysis.domain.TaskProposal;
import com.relaya.demo.analysis.port.out.AnalysisDraftRepositoryPort;
import com.relaya.demo.analysis.port.out.UsageLedgerPort;
import com.relaya.demo.audit.domain.AuditEvent;
import com.relaya.demo.audit.domain.AuditEventType;
import com.relaya.demo.audit.port.out.AuditEventPort;
import com.relaya.demo.board.domain.BoardWrite;
import com.relaya.demo.board.domain.IdempotencyKey;
import com.relaya.demo.board.port.out.BoardWriteRepositoryPort;
import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.domain.IntakeVersion;
import com.relaya.demo.intake.domain.ServiceType;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import com.relaya.demo.intake.port.out.IntakeVersionRepositoryPort;
import com.relaya.demo.review.domain.Approval;
import com.relaya.demo.review.port.out.ApprovalRepositoryPort;
import com.relaya.demo.tenant.port.out.TenantConfigPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class RepositoryAdaptersIntegrationTest {

	@Autowired
	private IntakeRepositoryPort intakeRepo;

	@Autowired
	private IntakeVersionRepositoryPort versionRepo;

	@Autowired
	private AnalysisDraftRepositoryPort draftRepo;

	@Autowired
	private ApprovalRepositoryPort approvalRepo;

	@Autowired
	private BoardWriteRepositoryPort boardWriteRepo;

	@Autowired
	private UsageLedgerPort usageLedgerPort;

	@Autowired
	private AuditEventPort auditPort;

	@Autowired
	private TenantConfigPort tenantConfigPort;

	private final UUID pilotTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private final UUID reviewerUserId = UUID.fromString("00000000-0000-0000-0000-000000000011");

	@Test
	@DisplayName("Roundtrip persistence across all domain entities and ports")
	void shouldPersistAndRetrieveAllEntities() {
		// 1. Tenant config seeded lookup
		Optional<String> modelConfig = tenantConfigPort.findConfigValue(pilotTenantId, "GROQ_MODEL");
		assertTrue(modelConfig.isPresent());
		assertEquals("llama-3.3-70b-versatile", modelConfig.get());

		// 2. Intake
		Intake intake = Intake.createNew(pilotTenantId, "Test Client", ServiceType.WEBSITE_DELIVERY);
		Intake savedIntake = intakeRepo.save(intake);
		assertNotNull(savedIntake);
		assertEquals("Test Client", intakeRepo.findById(pilotTenantId, intake.getId()).orElseThrow().getClientLabel());

		// 3. Intake Version
		IntakeVersion version = IntakeVersion.createInitial(pilotTenantId, intake.getId(), "Please build a marketing website.");
		IntakeVersion savedVersion = versionRepo.save(version);
		assertNotNull(savedVersion);
		assertEquals(1, versionRepo.findLatestByIntakeId(pilotTenantId, intake.getId()).orElseThrow().getVersionNumber());

		// 4. Analysis Draft with JSONB
		AnalysisDraft draft = AnalysisDraft.createNew(
				pilotTenantId, intake.getId(), version.getId(),
				List.of(new SourceExcerpt("Marketing website", "marketing website")),
				List.of(new SourceExcerpt("Hero section", "marketing website")),
				List.of(new SourceExcerpt("Brand guidelines", "marketing website")),
				"3 weeks", "$5,000",
				List.of("Domain registrar?"),
				List.of(new TaskProposal("Scaffold landing page", 2))
		);
		AnalysisDraft savedDraft = draftRepo.save(draft);
		assertNotNull(savedDraft);
		AnalysisDraft retrievedDraft = draftRepo.findById(pilotTenantId, draft.getId()).orElseThrow();
		assertEquals(1, retrievedDraft.getObjectives().size());
		assertEquals("Marketing website", retrievedDraft.getObjectives().get(0).text());

		// 5. Approval
		Approval approval = Approval.createNew(pilotTenantId, draft.getId(), reviewerUserId, retrievedDraft.getContentHash());
		Approval savedApproval = approvalRepo.save(approval);
		assertNotNull(savedApproval);
		assertTrue(approvalRepo.findLatestValidByDraftId(pilotTenantId, draft.getId()).isPresent());

		// 6. Board Write with Idempotency Key
		IdempotencyKey key = IdempotencyKey.of(approval.getId(), retrievedDraft.getContentHash());
		BoardWrite boardWrite = BoardWrite.createPending(pilotTenantId, approval.getId(), key);
		boardWrite.markStub("stub-123", "https://trello.com/c/stub-123");
		BoardWrite savedBoardWrite = boardWriteRepo.save(boardWrite);
		assertNotNull(savedBoardWrite);
		assertTrue(boardWriteRepo.findByIdempotencyKey(pilotTenantId, key).isPresent());

		// 7. Usage Ledger & Monthly Sum
		usageLedgerPort.recordUsage(pilotTenantId, intake.getId(), "GROQ", "llama-3.3-70b-versatile", 200, 100, new BigDecimal("0.000197"));
		BigDecimal mtdCost = usageLedgerPort.getMonthToDateCostUsd(pilotTenantId, YearMonth.now());
		assertTrue(mtdCost.compareTo(BigDecimal.ZERO) > 0);

		// 8. Audit Event
		auditPort.recordEvent(AuditEvent.create(pilotTenantId, intake.getId(), reviewerUserId, AuditEventType.DRAFT_APPROVED, "{\"status\":\"OK\"}"));
		List<AuditEvent> events = auditPort.findByIntakeId(pilotTenantId, intake.getId());
		assertEquals(1, events.size());
		assertEquals(AuditEventType.DRAFT_APPROVED, events.get(0).eventType());
	}
}