package com.relaya.demo.analysis.port.out;

import com.relaya.demo.analysis.domain.SourceExcerpt;
import com.relaya.demo.analysis.domain.TaskProposal;
import java.util.List;
import java.util.Objects;

public record ProviderExtractionResult(
		List<SourceExcerpt> objectives,
		List<SourceExcerpt> deliverables,
		List<SourceExcerpt> constraints,
		String timelineNotes,
		String budgetNotes,
		List<String> unknowns,
		List<TaskProposal> proposedTasks,
		String provider,
		String model,
		int promptTokens,
		int completionTokens
) {
	public ProviderExtractionResult {
		objectives = (objectives == null) ? List.of() : List.copyOf(objectives);
		deliverables = (deliverables == null) ? List.of() : List.copyOf(deliverables);
		constraints = (constraints == null) ? List.of() : List.copyOf(constraints);
		timelineNotes = (timelineNotes == null) ? "" : timelineNotes;
		budgetNotes = (budgetNotes == null) ? "" : budgetNotes;
		unknowns = (unknowns == null) ? List.of() : List.copyOf(unknowns);
		proposedTasks = (proposedTasks == null) ? List.of() : List.copyOf(proposedTasks);
		Objects.requireNonNull(provider, "provider must not be null");
		Objects.requireNonNull(model, "model must not be null");
		if (promptTokens < 0) {
			throw new IllegalArgumentException("promptTokens must be >= 0");
		}
		if (completionTokens < 0) {
			throw new IllegalArgumentException("completionTokens must be >= 0");
		}
	}
}