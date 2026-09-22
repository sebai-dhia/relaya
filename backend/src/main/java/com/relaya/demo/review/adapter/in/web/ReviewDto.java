package com.relaya.demo.review.adapter.in.web;

import com.relaya.demo.analysis.domain.SourceExcerpt;
import com.relaya.demo.analysis.domain.TaskProposal;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReviewDto {

	public record DraftResponse(
			UUID draftId,
			UUID intakeId,
			String contentHash,
			List<SourceExcerpt> objectives,
			List<SourceExcerpt> deliverables,
			List<SourceExcerpt> constraints,
			String timelineNotes,
			String budgetNotes,
			List<String> unknowns,
			List<TaskProposal> proposedTasks,
			boolean isApproved,
			boolean approvalInvalidated
	) {}

	public record UpdateDraftRequest(
			List<SourceExcerpt> objectives,
			List<SourceExcerpt> deliverables,
			List<SourceExcerpt> constraints,
			String timelineNotes,
			String budgetNotes,
			List<String> unknowns,
			List<TaskProposal> proposedTasks
	) {}

	public record UpdateDraftResponse(
			UUID draftId,
			String contentHash,
			boolean approvalInvalidated,
			Instant updatedAt
	) {}

	public record ApproveDraftRequest(
			String expectedContentHash
	) {}

	public record ApproveDraftResponse(
			UUID approvalId,
			UUID draftId,
			String contentHashAtApproval,
			Instant approvedAt
	) {}
}