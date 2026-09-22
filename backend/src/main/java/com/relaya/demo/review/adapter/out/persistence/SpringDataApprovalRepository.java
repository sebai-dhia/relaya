package com.relaya.demo.review.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataApprovalRepository extends JpaRepository<ApprovalJpaEntity, UUID> {

	Optional<ApprovalJpaEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<ApprovalJpaEntity> findFirstByTenantIdAndDraftIdOrderByApprovedAtDesc(UUID tenantId, UUID draftId);

	Optional<ApprovalJpaEntity> findFirstByTenantIdAndDraftIdAndInvalidatedAtIsNullOrderByApprovedAtDesc(UUID tenantId, UUID draftId);
}