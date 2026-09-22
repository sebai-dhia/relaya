package com.relaya.demo.board.adapter.out.persistence;

import com.relaya.demo.board.domain.WriteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataBoardWriteRepository extends JpaRepository<BoardWriteJpaEntity, UUID> {

	Optional<BoardWriteJpaEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<BoardWriteJpaEntity> findByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey);

	Optional<BoardWriteJpaEntity> findByTenantIdAndApprovalId(UUID tenantId, UUID approvalId);

	List<BoardWriteJpaEntity> findAllByTenantIdAndStatus(UUID tenantId, WriteStatus status);
}