package com.relaya.demo.review.adapter.in.web;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.domain.ContentHash;
import com.relaya.demo.analysis.port.out.AnalysisDraftRepositoryPort;
import com.relaya.demo.config.security.UserPrincipal;
import com.relaya.demo.review.domain.Approval;
import com.relaya.demo.review.port.in.ApproveDraftCommand;
import com.relaya.demo.review.port.in.UpdateDraftCommand;
import com.relaya.demo.review.port.out.ApprovalRepositoryPort;
import com.relaya.demo.review.service.ReviewDraftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drafts")
public class ReviewController {

	private final ReviewDraftService reviewDraftService;
	private final AnalysisDraftRepositoryPort draftRepo;
	private final ApprovalRepositoryPort approvalRepo;

	public ReviewController(
			ReviewDraftService reviewDraftService,
			AnalysisDraftRepositoryPort draftRepo,
			ApprovalRepositoryPort approvalRepo
	) {
		this.reviewDraftService = Objects.requireNonNull(reviewDraftService, "reviewDraftService must not be null");
		this.draftRepo = Objects.requireNonNull(draftRepo, "draftRepo must not be null");
		this.approvalRepo = Objects.requireNonNull(approvalRepo, "approvalRepo must not be null");
	}

	@GetMapping("/{id}")
	public ResponseEntity<ReviewDto.DraftResponse> getDraft(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id
	) {
		AnalysisDraft draft = draftRepo.findById(principal.tenantId(), id)
				.orElseThrow(() -> new NoSuchElementException("Draft not found: " + id));

		Optional<Approval> approvalOpt = approvalRepo.findByDraftId(principal.tenantId(), id);
		boolean isApproved = approvalOpt.map(a -> a.isValid() && a.matchesCurrentDraftHash(draft.getContentHash())).orElse(false);
		boolean approvalInvalidated = approvalOpt.map(a -> !a.isValid()).orElse(false);

		return ResponseEntity.ok(new ReviewDto.DraftResponse(
				draft.getId(),
				draft.getIntakeId(),
				draft.getContentHash().value(),
				draft.getObjectives(),
				draft.getDeliverables(),
				draft.getConstraints(),
				draft.getTimelineNotes(),
				draft.getBudgetNotes(),
				draft.getUnknowns(),
				draft.getProposedTasks(),
				isApproved,
				approvalInvalidated
		));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ReviewDto.UpdateDraftResponse> updateDraft(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id,
			@RequestBody ReviewDto.UpdateDraftRequest request
	) {
		UpdateDraftCommand command = new UpdateDraftCommand(
				principal.tenantId(),
				id,
				request.objectives(),
				request.deliverables(),
				request.constraints(),
				request.timelineNotes(),
				request.budgetNotes(),
				request.unknowns(),
				request.proposedTasks()
		);
		AnalysisDraft draft = reviewDraftService.updateDraft(command);
		return ResponseEntity.ok(new ReviewDto.UpdateDraftResponse(
				draft.getId(),
				draft.getContentHash().value(),
				true,
				draft.getUpdatedAt()
		));
	}

	@PostMapping("/{id}/approve")
	public ResponseEntity<ReviewDto.ApproveDraftResponse> approveDraft(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id,
			@RequestBody(required = false) ReviewDto.ApproveDraftRequest request
	) {
		ContentHash expectedHash;
		if (request != null && request.expectedContentHash() != null && !request.expectedContentHash().isBlank()) {
			expectedHash = new ContentHash(request.expectedContentHash());
		} else {
			AnalysisDraft draft = draftRepo.findById(principal.tenantId(), id)
					.orElseThrow(() -> new NoSuchElementException("Draft not found: " + id));
			expectedHash = draft.getContentHash();
		}

		ApproveDraftCommand command = new ApproveDraftCommand(
				principal.tenantId(),
				id,
				principal.userId(),
				expectedHash
		);
		Approval approval = reviewDraftService.approveDraft(command);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ReviewDto.ApproveDraftResponse(
				approval.getId(),
				approval.getDraftId(),
				approval.getContentHashAtApproval().value(),
				approval.getApprovedAt()
		));
	}
}