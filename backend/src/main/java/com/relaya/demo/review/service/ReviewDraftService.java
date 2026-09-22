package com.relaya.demo.review.service;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.port.out.AnalysisDraftRepositoryPort;
import com.relaya.demo.audit.domain.AuditEvent;
import com.relaya.demo.audit.domain.AuditEventType;
import com.relaya.demo.audit.port.out.AuditEventPort;
import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import com.relaya.demo.review.domain.Approval;
import com.relaya.demo.review.domain.DraftMismatchException;
import com.relaya.demo.review.port.in.ApproveDraftCommand;
import com.relaya.demo.review.port.in.ApproveDraftUseCase;
import com.relaya.demo.review.port.in.UpdateDraftCommand;
import com.relaya.demo.review.port.in.UpdateDraftUseCase;
import com.relaya.demo.review.port.out.ApprovalRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
public class ReviewDraftService implements UpdateDraftUseCase, ApproveDraftUseCase {

	private final AnalysisDraftRepositoryPort draftRepo;
	private final ApprovalRepositoryPort approvalRepo;
	private final IntakeRepositoryPort intakeRepo;
	private final AuditEventPort auditPort;

	public ReviewDraftService(
			AnalysisDraftRepositoryPort draftRepo,
			ApprovalRepositoryPort approvalRepo,
			IntakeRepositoryPort intakeRepo,
			AuditEventPort auditPort
	) {
		this.draftRepo = Objects.requireNonNull(draftRepo, "draftRepo must not be null");
		this.approvalRepo = Objects.requireNonNull(approvalRepo, "approvalRepo must not be null");
		this.intakeRepo = Objects.requireNonNull(intakeRepo, "intakeRepo must not be null");
		this.auditPort = Objects.requireNonNull(auditPort, "auditPort must not be null");
	}

	@Override
	@Transactional
	public AnalysisDraft updateDraft(UpdateDraftCommand command) {
		AnalysisDraft draft = draftRepo.findById(command.tenantId(), command.draftId())
				.orElseThrow(() -> new NoSuchElementException("Draft not found: " + command.draftId()));

		Optional<Approval> approvalOpt = approvalRepo.findByDraftId(command.tenantId(), command.draftId());
		if (approvalOpt.isPresent() && approvalOpt.get().isValid()) {
			Approval approval = approvalOpt.get();
			approval.invalidate();
			approvalRepo.save(approval);
			auditPort.recordEvent(AuditEvent.create(
					command.tenantId(), draft.getIntakeId(), null,
					AuditEventType.APPROVAL_INVALIDATED, "{\"reason\":\"Draft updated by reviewer\"}"
			));
		}

		draft.updateContent(
				command.objectives(),
				command.deliverables(),
				command.constraints(),
				command.timelineNotes(),
				command.budgetNotes(),
				command.unknowns(),
				command.proposedTasks()
		);
		AnalysisDraft savedDraft = draftRepo.save(draft);

		Optional<Intake> intakeOpt = intakeRepo.findById(command.tenantId(), draft.getIntakeId());
		if (intakeOpt.isPresent()) {
			Intake intake = intakeOpt.get();
			intake.markDraftEdited();
			intakeRepo.save(intake);
		}

		auditPort.recordEvent(AuditEvent.create(
				command.tenantId(), draft.getIntakeId(), null,
				AuditEventType.DRAFT_UPDATED, null
		));

		return savedDraft;
	}

	@Override
	@Transactional
	public Approval approveDraft(ApproveDraftCommand command) {
		AnalysisDraft draft = draftRepo.findById(command.tenantId(), command.draftId())
				.orElseThrow(() -> new NoSuchElementException("Draft not found: " + command.draftId()));

		if (!draft.getContentHash().equals(command.expectedContentHash())) {
			throw new DraftMismatchException("Draft content hash does not match expected hash at approval");
		}

		Approval approval = Approval.createNew(
				command.tenantId(),
				command.draftId(),
				command.reviewerUserId(),
				draft.getContentHash()
		);
		Approval savedApproval = approvalRepo.save(approval);

		Optional<Intake> intakeOpt = intakeRepo.findById(command.tenantId(), draft.getIntakeId());
		if (intakeOpt.isPresent()) {
			Intake intake = intakeOpt.get();
			intake.markApproved();
			intakeRepo.save(intake);
		}

		auditPort.recordEvent(AuditEvent.create(
				command.tenantId(), draft.getIntakeId(), command.reviewerUserId(),
				AuditEventType.DRAFT_APPROVED, null
		));

		return savedApproval;
	}
}