package com.relaya.demo.analysis.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AnalysisDraft {

	private final UUID id;
	private final UUID tenantId;
	private final UUID intakeId;
	private final UUID intakeVersionId;
	private List<SourceExcerpt> objectives;
	private List<SourceExcerpt> deliverables;
	private List<SourceExcerpt> constraints;
	private String timelineNotes;
	private String budgetNotes;
	private List<String> unknowns;
	private List<TaskProposal> proposedTasks;
	private ContentHash contentHash;
	private final Instant createdAt;
	private Instant updatedAt;

	public AnalysisDraft(
			UUID id,
			UUID tenantId,
			UUID intakeId,
			UUID intakeVersionId,
			List<SourceExcerpt> objectives,
			List<SourceExcerpt> deliverables,
			List<SourceExcerpt> constraints,
			String timelineNotes,
			String budgetNotes,
			List<String> unknowns,
			List<TaskProposal> proposedTasks,
			ContentHash contentHash,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
		this.intakeId = Objects.requireNonNull(intakeId, "intakeId must not be null");
		this.intakeVersionId = Objects.requireNonNull(intakeVersionId, "intakeVersionId must not be null");
		this.objectives = copyExcerpts(objectives);
		this.deliverables = copyExcerpts(deliverables);
		this.constraints = copyExcerpts(constraints);
		this.timelineNotes = (timelineNotes == null) ? "" : timelineNotes;
		this.budgetNotes = (budgetNotes == null) ? "" : budgetNotes;
		this.unknowns = copyStrings(unknowns);
		this.proposedTasks = copyTasks(proposedTasks);
		this.contentHash = (contentHash != null) ? contentHash : computeHash();
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
		this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
	}

	public static AnalysisDraft createNew(
			UUID tenantId,
			UUID intakeId,
			UUID intakeVersionId,
			List<SourceExcerpt> objectives,
			List<SourceExcerpt> deliverables,
			List<SourceExcerpt> constraints,
			String timelineNotes,
			String budgetNotes,
			List<String> unknowns,
			List<TaskProposal> proposedTasks
	) {
		Instant now = Instant.now();
		ContentHash hash = ContentHash.compute(
				objectives, deliverables, constraints,
				unknowns, proposedTasks, timelineNotes, budgetNotes
		);
		return new AnalysisDraft(
				UUID.randomUUID(), tenantId, intakeId, intakeVersionId,
				objectives, deliverables, constraints, timelineNotes, budgetNotes,
				unknowns, proposedTasks, hash, now, now
		);
	}

	public void updateContent(
			List<SourceExcerpt> objectives,
			List<SourceExcerpt> deliverables,
			List<SourceExcerpt> constraints,
			String timelineNotes,
			String budgetNotes,
			List<String> unknowns,
			List<TaskProposal> proposedTasks
	) {
		this.objectives = copyExcerpts(objectives);
		this.deliverables = copyExcerpts(deliverables);
		this.constraints = copyExcerpts(constraints);
		this.timelineNotes = (timelineNotes == null) ? "" : timelineNotes;
		this.budgetNotes = (budgetNotes == null) ? "" : budgetNotes;
		this.unknowns = copyStrings(unknowns);
		this.proposedTasks = copyTasks(proposedTasks);
		this.contentHash = computeHash();
		this.updatedAt = Instant.now();
	}

	public ContentHash computeHash() {
		return ContentHash.compute(
				objectives, deliverables, constraints,
				unknowns, proposedTasks, timelineNotes, budgetNotes
		);
	}

	private static List<SourceExcerpt> copyExcerpts(List<SourceExcerpt> in) {
		return (in == null) ? new ArrayList<>() : new ArrayList<>(in);
	}

	private static List<String> copyStrings(List<String> in) {
		return (in == null) ? new ArrayList<>() : new ArrayList<>(in);
	}

	private static List<TaskProposal> copyTasks(List<TaskProposal> in) {
		return (in == null) ? new ArrayList<>() : new ArrayList<>(in);
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public UUID getIntakeId() {
		return intakeId;
	}

	public UUID getIntakeVersionId() {
		return intakeVersionId;
	}

	public List<SourceExcerpt> getObjectives() {
		return Collections.unmodifiableList(objectives);
	}

	public List<SourceExcerpt> getDeliverables() {
		return Collections.unmodifiableList(deliverables);
	}

	public List<SourceExcerpt> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public String getTimelineNotes() {
		return timelineNotes;
	}

	public String getBudgetNotes() {
		return budgetNotes;
	}

	public List<String> getUnknowns() {
		return Collections.unmodifiableList(unknowns);
	}

	public List<TaskProposal> getProposedTasks() {
		return Collections.unmodifiableList(proposedTasks);
	}

	public ContentHash getContentHash() {
		return contentHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AnalysisDraft that)) return false;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}