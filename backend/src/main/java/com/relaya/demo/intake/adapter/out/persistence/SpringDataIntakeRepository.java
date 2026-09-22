package com.relaya.demo.intake.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataIntakeRepository extends JpaRepository<IntakeJpaEntity, UUID> {

	Optional<IntakeJpaEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	List<IntakeJpaEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

	Page<IntakeJpaEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

	Page<IntakeJpaEntity> findAllByTenantIdAndStatusOrderByCreatedAtDesc(UUID tenantId, String status, Pageable pageable);

	boolean existsByTenantIdAndId(UUID tenantId, UUID id);
 
 	void deleteByTenantIdAndId(UUID tenantId, UUID id);
 }