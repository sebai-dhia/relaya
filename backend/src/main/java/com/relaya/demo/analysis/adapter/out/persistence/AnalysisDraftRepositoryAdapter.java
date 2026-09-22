package com.relaya.demo.analysis.adapter.out.persistence;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.port.out.AnalysisDraftRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class AnalysisDraftRepositoryAdapter implements AnalysisDraftRepositoryPort {

	private final SpringDataAnalysisDraftRepository repository;

	public AnalysisDraftRepositoryAdapter(SpringDataAnalysisDraftRepository repository) {
		this.repository = Objects.requireNonNull(repository, "repository must not be null");
	}

	@Override
	public AnalysisDraft save(AnalysisDraft draft) {
		Objects.requireNonNull(draft, "draft must not be null");
		AnalysisDraftJpaEntity entity = AnalysisDraftJpaEntity.fromDomain(draft);
		return repository.save(entity).toDomain();
	}

	@Override
	public Optional<AnalysisDraft> findById(UUID tenantId, UUID draftId) {
		if (tenantId == null || draftId == null) {
			return Optional.empty();
		}
		return repository.findByTenantIdAndId(tenantId, draftId).map(AnalysisDraftJpaEntity::toDomain);
	}

	@Override
	public Optional<AnalysisDraft> findByIntakeId(UUID tenantId, UUID intakeId) {
		if (tenantId == null || intakeId == null) {
			return Optional.empty();
		}
		return repository.findByTenantIdAndIntakeId(tenantId, intakeId).map(AnalysisDraftJpaEntity::toDomain);
	}

	@Override
	public Optional<AnalysisDraft> findByIntakeVersionId(UUID tenantId, UUID intakeVersionId) {
		if (tenantId == null || intakeVersionId == null) {
			return Optional.empty();
		}
		return repository.findByTenantIdAndIntakeVersionId(tenantId, intakeVersionId).map(AnalysisDraftJpaEntity::toDomain);
	}
}