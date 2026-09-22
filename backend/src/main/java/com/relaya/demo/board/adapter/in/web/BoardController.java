package com.relaya.demo.board.adapter.in.web;

import com.relaya.demo.board.domain.BoardWrite;
import com.relaya.demo.board.domain.IdempotencyKey;
import com.relaya.demo.board.domain.WriteStatus;
import com.relaya.demo.board.port.in.TriggerBoardWriteCommand;
import com.relaya.demo.board.service.BoardWriteService;
import com.relaya.demo.config.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/approvals")
public class BoardController {

	private final BoardWriteService boardWriteService;

	public BoardController(BoardWriteService boardWriteService) {
		this.boardWriteService = Objects.requireNonNull(boardWriteService, "boardWriteService must not be null");
	}

	@PostMapping("/{id}/write")
	public ResponseEntity<BoardDto.BoardWriteResponse> triggerBoardWrite(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader
	) {
		IdempotencyKey key = (idempotencyHeader != null && !idempotencyHeader.isBlank())
				? new IdempotencyKey(idempotencyHeader.trim())
				: new IdempotencyKey(UUID.randomUUID().toString());

		TriggerBoardWriteCommand command = new TriggerBoardWriteCommand(principal.tenantId(), id, key);
		BoardWrite write = boardWriteService.triggerBoardWrite(command);

		String message = (write.getStatus() == WriteStatus.STUB)
				? "Stub connector — card would be created here"
				: null;

		return ResponseEntity.status(HttpStatus.CREATED).body(new BoardDto.BoardWriteResponse(
				write.getId(),
				write.getStatus().name(),
				write.getDestinationBoard(),
				write.getTrelloCardId(),
				write.getTrelloCardUrl(),
				message
		));
	}
}