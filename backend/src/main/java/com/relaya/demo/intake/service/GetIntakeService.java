package com.relaya.demo.intake.service;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.port.out.AnalysisDraftRepositoryPort;
import com.relaya.demo.audit.domain.AuditEvent;
import com.relaya.demo.audit.port.out.AuditEventPort;
import com.relaya.demo.intake.adapter.out.persistence.IntakeJpaEntity;
import com.relaya.demo.intake.adapter.out.persistence.SpringDataIntakeRepository;
import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.domain.IntakeVersion;
import com.relaya.demo.intake.port.in.GetIntakeQuery;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import com.relaya.demo.intake.port.out.IntakeVersionRepositoryPort;
import com.relaya.demo.review.domain.Approval;
import com.relaya.demo.review.port.out.ApprovalRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetIntakeService implements GetIntakeQuery {

	private final IntakeRepositoryPort intakeRepo;
	private final SpringDataIntakeRepository springDataIntakeRepo;
	private final IntakeVersionRepositoryPort versionRepo;
	private final AnalysisDraftRepositoryPort draftRepo;
	private final ApprovalRepositoryPort approvalRepo;
	private final AuditEventPort auditPort;

	public GetIntakeService(
			IntakeRepositoryPort intakeRepo,
			SpringDataIntakeRepository springDataIntakeRepo,
			IntakeVersionRepositoryPort versionRepo,
			AnalysisDraftRepositoryPort draftRepo,
			ApprovalRepositoryPort approvalRepo,
			AuditEventPort auditPort
	) {
		this.intakeRepo = Objects.requireNonNull(intakeRepo, "intakeRepo must not be null");
		this.springDataIntakeRepo = Objects.requireNonNull(springDataIntakeRepo, "springDataIntakeRepo must not be null");
		this.versionRepo = Objects.requireNonNull(versionRepo, "versionRepo must not be null");
		this.draftRepo = Objects.requireNonNull(draftRepo, "draftRepo must not be null");
		this.approvalRepo = Objects.requireNonNull(approvalRepo, "approvalRepo must not be null");
		this.auditPort = Objects.requireNonNull(auditPort, "auditPort must not be null");
	}

	@Override
	public Optional<Intake> getIntake(UUID tenantId, UUID intakeId) {
		return intakeRepo.findById(tenantId, intakeId);
	}

	public Page<IntakeJpaEntity> listIntakes(UUID tenantId, String status, int page, int size) {
		PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(50, Math.max(1, size)));
		if (status != null && !status.isBlank()) {
			return springDataIntakeRepo.findAllByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status.trim(), pageable);
		}
		return springDataIntakeRepo.findAllByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
	}

	public IntakeDetails getIntakeDetails(UUID tenantId, UUID intakeId) {
		Intake intake = intakeRepo.findById(tenantId, intakeId)
				.orElseThrow(() -> new NoSuchElementException("Intake not found: " + intakeId));

		int currentVersion = versionRepo.findLatestByIntakeId(tenantId, intakeId)
				.map(IntakeVersion::getVersionNumber)
				.orElse(1);

		Optional<AnalysisDraft> draftOpt = draftRepo.findByIntakeId(tenantId, intakeId);
		UUID draftId = draftOpt.map(AnalysisDraft::getId).orElse(null);

		UUID approvalId = null;
		boolean approvalInvalidated = false;

		if (draftId != null) {
			Optional<Approval> approvalOpt = approvalRepo.findByDraftId(tenantId, draftId);
			if (approvalOpt.isPresent()) {
				Approval app = approvalOpt.get();
				approvalId = app.getId();
				approvalInvalidated = !app.isValid();
			}
		}

		return new IntakeDetails(
				intake.getId(),
				intake.getClientLabel(),
				intake.getStatus().name(),
				currentVersion,
				draftId,
				approvalId,
				approvalInvalidated
		);
	}

	public List<AuditEvent> getIntakeAuditTrail(UUID tenantId, UUID intakeId) {
		if (!intakeRepo.existsById(tenantId, intakeId)) {
			throw new NoSuchElementException("Intake not found: " + intakeId);
		}
		return auditPort.findByIntakeId(tenantId, intakeId);
	}

	public record IntakeDetails(
			UUID intakeId,
			String clientLabel,
			String status,
			int currentVersion,
			UUID draftId,
			UUID approvalId,
			boolean approvalInvalidated
	) {}
}