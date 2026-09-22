package com.relaya.demo.intake.adapter.out.persistence;

import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class IntakeRepositoryAdapter implements IntakeRepositoryPort {

	private final SpringDataIntakeRepository repository;

	public IntakeRepositoryAdapter(SpringDataIntakeRepository repository) {
		this.repository = Objects.requireNonNull(repository, "repository must not be null");
	}

	@Override
	public Intake save(Intake intake) {
		Objects.requireNonNull(intake, "intake must not be null");
		IntakeJpaEntity entity = IntakeJpaEntity.fromDomain(intake);
		IntakeJpaEntity saved = repository.save(entity);
		return saved.toDomain();
	}

	@Override
	public Optional<Intake> findById(UUID tenantId, UUID intakeId) {
		if (tenantId == null || intakeId == null) {
			return Optional.empty();
		}
		return repository.findByTenantIdAndId(tenantId, intakeId).map(IntakeJpaEntity::toDomain);
	}

	@Override
	public List<Intake> findAllByTenantId(UUID tenantId) {
		if (tenantId == null) {
			return List.of();
		}
		return repository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream()
				.map(IntakeJpaEntity::toDomain)
				.toList();
	}

	@Override
	public boolean existsById(UUID tenantId, UUID intakeId) {
		if (tenantId == null || intakeId == null) {
			return false;
		}
		return repository.existsByTenantIdAndId(tenantId, intakeId);
	}

	@Override
	public void delete(UUID tenantId, UUID intakeId) {
		if (tenantId == null || intakeId == null) {
			return;
		}
		repository.findByTenantIdAndId(tenantId, intakeId).ifPresent(repository::delete);
	}
}