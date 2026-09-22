package com.relaya.demo.board.port.out;

import com.relaya.demo.board.domain.BoardWrite;
import com.relaya.demo.board.domain.IdempotencyKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardWriteRepositoryPort {

	BoardWrite save(BoardWrite boardWrite);

	Optional<BoardWrite> findById(UUID tenantId, UUID id);

	Optional<BoardWrite> findByIdempotencyKey(UUID tenantId, IdempotencyKey idempotencyKey);

	Optional<BoardWrite> findByApprovalId(UUID tenantId, UUID approvalId);

	List<BoardWrite> findFailedWrites(UUID tenantId);
}