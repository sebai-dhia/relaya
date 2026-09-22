package com.relaya.demo.audit.adapter.out.persistence;

import com.relaya.demo.audit.domain.AuditEvent;
import com.relaya.demo.audit.port.out.AuditEventPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class AuditEventJpaAdapter implements AuditEventPort {

	private final SpringDataAuditEventRepository repository;

	public AuditEventJpaAdapter(SpringDataAuditEventRepository repository) {
		this.repository = Objects.requireNonNull(repository, "repository must not be null");
	}

	@Override
	public void recordEvent(AuditEvent event) {
		Objects.requireNonNull(event, "event must not be null");
		AuditEventJpaEntity entity = AuditEventJpaEntity.fromDomain(event);
		repository.save(entity);
	}

	@Override
	public List<AuditEvent> findByIntakeId(UUID tenantId, UUID intakeId) {
		if (tenantId == null || intakeId == null) {
			return List.of();
		}
		return repository.findAllByTenantIdAndIntakeIdOrderByOccurredAtDesc(tenantId, intakeId).stream()
				.map(AuditEventJpaEntity::toDomain)
				.toList();
	}

	@Override
	public List<AuditEvent> findByTenantId(UUID tenantId) {
		if (tenantId == null) {
			return List.of();
		}
		return repository.findAllByTenantIdOrderByOccurredAtDesc(tenantId).stream()
				.map(AuditEventJpaEntity::toDomain)
				.toList();
	}
}