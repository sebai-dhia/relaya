package com.relaya.demo.review.port.out;

import com.relaya.demo.review.domain.Approval;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalRepositoryPort {

	Approval save(Approval approval);

	Optional<Approval> findById(UUID tenantId, UUID approvalId);

	Optional<Approval> findByDraftId(UUID tenantId, UUID draftId);

	Optional<Approval> findLatestValidByDraftId(UUID tenantId, UUID draftId);
}