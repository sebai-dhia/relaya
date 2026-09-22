package com.relaya.demo.board.adapter.out.persistence;

import com.relaya.demo.board.domain.BoardWrite;
import com.relaya.demo.board.domain.IdempotencyKey;
import com.relaya.demo.board.domain.WriteStatus;
import com.relaya.demo.board.port.out.BoardWriteRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class BoardWriteRepositoryAdapter implements BoardWriteRepositoryPort {

	private final SpringDataBoardWriteRepository repository;

	public BoardWriteRepositoryAdapter(SpringDataBoardWriteRepository repository) {
		this.repository = Objects.requireNonNull(repository, "repository must not be null");
	}

	@Override
	public BoardWrite save(BoardWrite boardWrite) {
		Objects.requireNonNull(boardWrite, "boardWrite must not be null");
		BoardWriteJpaEntity entity = BoardWriteJpaEntity.fromDomain(boardWrite);
		return repository.save(entity).toDomain();
	}

	@Override
	public Optional<BoardWrite> findById(UUID tenantId, UUID id) {
		if (tenantId == null || id == null) {
			return Optional.empty();
		}
		return repository.findByTenantIdAndId(tenantId, id).map(BoardWriteJpaEntity::toDomain);
	}

	@Override
	public Optional<BoardWrite> findByIdempotencyKey(UUID tenantId, IdempotencyKey idempotencyKey) {
		if (tenantId == null || idempotencyKey == null) {
			return Optional.empty();
		}
		return repository.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey.value())
				.map(BoardWriteJpaEntity::toDomain);
	}

	@Override
	public Optional<BoardWrite> findByApprovalId(UUID tenantId, UUID approvalId) {
		if (tenantId == null || approvalId == null) {
			return Optional.empty();
		}
		return repository.findByTenantIdAndApprovalId(tenantId, approvalId)
				.map(BoardWriteJpaEntity::toDomain);
	}

	@Override
	public List<BoardWrite> findFailedWrites(UUID tenantId) {
		if (tenantId == null) {
			return List.of();
		}
		return repository.findAllByTenantIdAndStatus(tenantId, WriteStatus.FAILED).stream()
				.map(BoardWriteJpaEntity::toDomain)
				.toList();
	}
}