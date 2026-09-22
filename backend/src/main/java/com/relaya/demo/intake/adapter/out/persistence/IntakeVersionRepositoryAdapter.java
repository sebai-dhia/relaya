package com.relaya.demo.intake.adapter.out.persistence;

import com.relaya.demo.intake.domain.IntakeVersion;
import com.relaya.demo.intake.port.out.IntakeVersionRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class IntakeVersionRepositoryAdapter implements IntakeVersionRepositoryPort {

	private final SpringDataIntakeVersionRepository repository;

	public IntakeVersionRepositoryAdapter(SpringDataIntakeVersionRepository repository) {
		this.repository = Objects.requireNonNull(repository, "repository must not be null");
	}

	@Override
	public IntakeVersion save(IntakeVersion version) {
		Objects.requireNonNull(version, "version must not be null");
		IntakeVersionJpaEntity entity = IntakeVersionJpaEntity.fromDomain(version);
		return repository.save(entity).toDomain();
	}

	@Override
	public Optional<IntakeVersion> findLatestByIntakeId(UUID tenantId, UUID intakeId) {
		if (tenantId == null || intakeId == null) {
			return Optional.empty();
		}
		return repository.findFirstByTenantIdAndIntakeIdOrderByVersionNumberDesc(tenantId, intakeId)
				.map(IntakeVersionJpaEntity::toDomain);
	}

	@Override
	public Optional<IntakeVersion> findByIntakeIdAndVersion(UUID tenantId, UUID intakeId, int versionNumber) {
		if (tenantId == null || intakeId == null) {
			return Optional.empty();
		}
		return repository.findByTenantIdAndIntakeIdAndVersionNumber(tenantId, intakeId, versionNumber)
				.map(IntakeVersionJpaEntity::toDomain);
	}

	@Override
	public List<IntakeVersion> findAllByIntakeId(UUID tenantId, UUID intakeId) {
		if (tenantId == null || intakeId == null) {
			return List.of();
		}
		return repository.findAllByTenantIdAndIntakeIdOrderByVersionNumberAsc(tenantId, intakeId).stream()
				.map(IntakeVersionJpaEntity::toDomain)
				.toList();
	}
}