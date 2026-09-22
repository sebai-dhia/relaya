package com.relaya.demo.board.service;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.port.out.AnalysisDraftRepositoryPort;
import com.relaya.demo.audit.domain.AuditEvent;
import com.relaya.demo.audit.domain.AuditEventType;
import com.relaya.demo.audit.port.out.AuditEventPort;
import com.relaya.demo.board.domain.BoardWrite;
import com.relaya.demo.board.domain.WriteStatus;
import com.relaya.demo.board.port.in.TriggerBoardWriteCommand;
import com.relaya.demo.board.port.in.TriggerBoardWriteUseCase;
import com.relaya.demo.board.port.out.BoardConnector;
import com.relaya.demo.board.port.out.BoardWriteContext;
import com.relaya.demo.board.port.out.BoardWriteRepositoryPort;
import com.relaya.demo.board.port.out.BoardWriteResult;
import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import com.relaya.demo.review.domain.Approval;
import com.relaya.demo.review.domain.DraftMismatchException;
import com.relaya.demo.review.port.out.ApprovalRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class BoardWriteService implements TriggerBoardWriteUseCase {

	private final BoardWriteRepositoryPort boardWriteRepo;
	private final ApprovalRepositoryPort approvalRepo;
	private final AnalysisDraftRepositoryPort draftRepo;
	private final IntakeRepositoryPort intakeRepo;
	private final BoardConnector boardConnector;
	private final AuditEventPort auditPort;

	public BoardWriteService(
			BoardWriteRepositoryPort boardWriteRepo,
			ApprovalRepositoryPort approvalRepo,
			AnalysisDraftRepositoryPort draftRepo,
			IntakeRepositoryPort intakeRepo,
			BoardConnector boardConnector,
			AuditEventPort auditPort
	) {
		this.boardWriteRepo = Objects.requireNonNull(boardWriteRepo, "boardWriteRepo must not be null");
		this.approvalRepo = Objects.requireNonNull(approvalRepo, "approvalRepo must not be null");
		this.draftRepo = Objects.requireNonNull(draftRepo, "draftRepo must not be null");
		this.intakeRepo = Objects.requireNonNull(intakeRepo, "intakeRepo must not be null");
		this.boardConnector = Objects.requireNonNull(boardConnector, "boardConnector must not be null");
		this.auditPort = Objects.requireNonNull(auditPort, "auditPort must not be null");
	}

	@Override
	@Transactional
	public BoardWrite triggerBoardWrite(TriggerBoardWriteCommand command) {
		UUID tenantId = command.tenantId();
		UUID approvalId = command.approvalId();

		Optional<BoardWrite> existingByApproval = boardWriteRepo.findByApprovalId(tenantId, approvalId);
		if (existingByApproval.isPresent()) {
			return existingByApproval.get();
		}

		Approval approval = approvalRepo.findById(tenantId, approvalId)
				.orElseThrow(() -> new NoSuchElementException("Approval not found: " + approvalId));

		if (!approval.isValid()) {
			throw new DraftMismatchException("Approval has been invalidated and cannot be written to board");
		}

		AnalysisDraft draft = draftRepo.findById(tenantId, approval.getDraftId())
				.orElseThrow(() -> new NoSuchElementException("Draft not found: " + approval.getDraftId()));

		if (!approval.matchesCurrentDraftHash(draft.getContentHash())) {
			throw new DraftMismatchException("Draft content hash has changed since approval");
		}

		Intake intake = intakeRepo.findById(tenantId, draft.getIntakeId())
				.orElseThrow(() -> new NoSuchElementException("Intake not found: " + draft.getIntakeId()));

		intake.markWritePending();
		intakeRepo.save(intake);

		BoardWrite boardWrite = BoardWrite.createPending(tenantId, approvalId, command.idempotencyKey());
		boardWriteRepo.save(boardWrite);

		BoardWriteContext context = new BoardWriteContext(
				tenantId, approvalId, intake.getClientLabel(), draft, command.idempotencyKey()
		);

		BoardWriteResult result = boardConnector.writeCard(context);

		if (result.status() == WriteStatus.SUCCESS) {
			boardWrite.markSuccess(result.trelloCardId(), result.trelloCardUrl());
			intake.markWriteSuccess();
			recordAuditEvent(tenantId, intake.getId(), AuditEventType.BOARD_WRITE_COMPLETED, null);
		} else if (result.status() == WriteStatus.STUB) {
			boardWrite.markStub(result.trelloCardId(), result.trelloCardUrl());
			intake.markWriteSuccess();
			recordAuditEvent(tenantId, intake.getId(), AuditEventType.BOARD_WRITE_COMPLETED, "{\"mode\":\"STUB\"}");
		} else {
			boardWrite.markFailed(result.errorDetails());
			intake.markWriteFailed();
			String safeError = result.errorDetails() != null ? result.errorDetails().replace("\"", "\\\"") : "Unknown error";
			recordAuditEvent(tenantId, intake.getId(), AuditEventType.BOARD_WRITE_FAILED, "{\"error\":\"" + safeError + "\"}");
		}

		intakeRepo.save(intake);
		return boardWriteRepo.save(boardWrite);
	}

	private void recordAuditEvent(UUID tenantId, UUID intakeId, AuditEventType eventType, String payload) {
		auditPort.recordEvent(AuditEvent.create(tenantId, intakeId, null, eventType, payload));
	}
}