package com.relaya.demo.analysis.adapter.out.persistence;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.domain.ContentHash;
import com.relaya.demo.analysis.domain.SourceExcerpt;
import com.relaya.demo.analysis.domain.TaskProposal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "analysis_drafts")
public class AnalysisDraftJpaEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "intake_id", nullable = false)
	private UUID intakeId;

	@Column(name = "intake_version_id", nullable = false, unique = true)
	private UUID intakeVersionId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "objectives", nullable = false, columnDefinition = "jsonb")
	private List<SourceExcerpt> objectives;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "deliverables", nullable = false, columnDefinition = "jsonb")
	private List<SourceExcerpt> deliverables;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "constraints", nullable = false, columnDefinition = "jsonb")
	private List<SourceExcerpt> constraints;

	@Column(name = "timeline_notes", columnDefinition = "TEXT")
	private String timelineNotes;

	@Column(name = "budget_notes", columnDefinition = "TEXT")
	private String budgetNotes;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "unknowns", nullable = false, columnDefinition = "jsonb")
	private List<String> unknowns;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "proposed_tasks", nullable = false, columnDefinition = "jsonb")
	private List<TaskProposal> proposedTasks;

	@Column(name = "content_hash", nullable = false, length = 64)
	private String contentHash;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected AnalysisDraftJpaEntity() {
	}

	public AnalysisDraftJpaEntity(
			UUID id, UUID tenantId, UUID intakeId, UUID intakeVersionId,
			List<SourceExcerpt> objectives, List<SourceExcerpt> deliverables,
			List<SourceExcerpt> constraints, String timelineNotes, String budgetNotes,
			List<String> unknowns, List<TaskProposal> proposedTasks, String contentHash,
			Instant createdAt, Instant updatedAt
	) {
		this.id = id;
		this.tenantId = tenantId;
		this.intakeId = intakeId;
		this.intakeVersionId = intakeVersionId;
		this.objectives = objectives;
		this.deliverables = deliverables;
		this.constraints = constraints;
		this.timelineNotes = timelineNotes;
		this.budgetNotes = budgetNotes;
		this.unknowns = unknowns;
		this.proposedTasks = proposedTasks;
		this.contentHash = contentHash;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static AnalysisDraftJpaEntity fromDomain(AnalysisDraft domain) {
		return new AnalysisDraftJpaEntity(
				domain.getId(),
				domain.getTenantId(),
				domain.getIntakeId(),
				domain.getIntakeVersionId(),
				domain.getObjectives(),
				domain.getDeliverables(),
				domain.getConstraints(),
				domain.getTimelineNotes(),
				domain.getBudgetNotes(),
				domain.getUnknowns(),
				domain.getProposedTasks(),
				domain.getContentHash().value(),
				domain.getCreatedAt(),
				domain.getUpdatedAt()
		);
	}

	public AnalysisDraft toDomain() {
		return new AnalysisDraft(
				id, tenantId, intakeId, intakeVersionId,
				objectives, deliverables, constraints, timelineNotes, budgetNotes,
				unknowns, proposedTasks, new ContentHash(contentHash), createdAt, updatedAt
		);
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
		return objectives;
	}

	public List<SourceExcerpt> getDeliverables() {
		return deliverables;
	}

	public List<SourceExcerpt> getConstraints() {
		return constraints;
	}

	public String getTimelineNotes() {
		return timelineNotes;
	}

	public String getBudgetNotes() {
		return budgetNotes;
	}

	public List<String> getUnknowns() {
		return unknowns;
	}

	public List<TaskProposal> getProposedTasks() {
		return proposedTasks;
	}

	public String getContentHash() {
		return contentHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}