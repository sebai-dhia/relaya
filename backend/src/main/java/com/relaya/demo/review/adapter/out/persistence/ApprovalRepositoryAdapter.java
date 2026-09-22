package com.relaya.demo.review.adapter.out.persistence;

import com.relaya.demo.review.domain.Approval;
import com.relaya.demo.review.port.out.ApprovalRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class ApprovalRepositoryAdapter implements ApprovalRepositoryPort {

	private final SpringDataApprovalRepository repository;

	public ApprovalRepositoryAdapter(SpringDataApprovalRepository repository) {
		this.repository = Objects.requireNonNull(repository, "repository must not be null");
	}

	@Override
	public Approval save(Approval approval) {
		Objects.requireNonNull(approval, "approval must not be null");
		ApprovalJpaEntity entity = ApprovalJpaEntity.fromDomain(approval);
		return repository.save(entity).toDomain();
	}

	@Override
	public Optional<Approval> findById(UUID tenantId, UUID approvalId) {
		if (tenantId == null || approvalId == null) {
			return Optional.empty();
		}
		return repository.findByTenantIdAndId(tenantId, approvalId).map(ApprovalJpaEntity::toDomain);
	}

	@Override
	public Optional<Approval> findByDraftId(UUID tenantId, UUID draftId) {
		if (tenantId == null || draftId == null) {
			return Optional.empty();
		}
		return repository.findFirstByTenantIdAndDraftIdOrderByApprovedAtDesc(tenantId, draftId)
				.map(ApprovalJpaEntity::toDomain);
	}

	@Override
	public Optional<Approval> findLatestValidByDraftId(UUID tenantId, UUID draftId) {
		if (tenantId == null || draftId == null) {
			return Optional.empty();
		}
		return repository.findFirstByTenantIdAndDraftIdAndInvalidatedAtIsNullOrderByApprovedAtDesc(tenantId, draftId)
				.map(ApprovalJpaEntity::toDomain);
	}
}