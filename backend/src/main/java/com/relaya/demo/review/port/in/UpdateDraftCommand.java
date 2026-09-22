package com.relaya.demo.review.port.in;

import com.relaya.demo.analysis.domain.SourceExcerpt;
import com.relaya.demo.analysis.domain.TaskProposal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record UpdateDraftCommand(
		UUID tenantId,
		UUID draftId,
		List<SourceExcerpt> objectives,
		List<SourceExcerpt> deliverables,
		List<SourceExcerpt> constraints,
		String timelineNotes,
		String budgetNotes,
		List<String> unknowns,
		List<TaskProposal> proposedTasks
) {
	public UpdateDraftCommand {
		Objects.requireNonNull(tenantId, "tenantId must not be null");
		Objects.requireNonNull(draftId, "draftId must not be null");
		objectives = (objectives == null) ? List.of() : List.copyOf(objectives);
		deliverables = (deliverables == null) ? List.of() : List.copyOf(deliverables);
		constraints = (constraints == null) ? List.of() : List.copyOf(constraints);
		timelineNotes = (timelineNotes == null) ? "" : timelineNotes;
		budgetNotes = (budgetNotes == null) ? "" : budgetNotes;
		unknowns = (unknowns == null) ? List.of() : List.copyOf(unknowns);
		proposedTasks = (proposedTasks == null) ? List.of() : List.copyOf(proposedTasks);
	}
}