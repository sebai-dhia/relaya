package com.relaya.demo.board.port.out;

import com.relaya.demo.board.domain.WriteStatus;
import java.util.Objects;

public record BoardWriteResult(
		WriteStatus status,
		String trelloCardId,
		String trelloCardUrl,
		String errorDetails
) {
	public BoardWriteResult {
		Objects.requireNonNull(status, "status must not be null");
	}

	public static BoardWriteResult success(String trelloCardId, String trelloCardUrl) {
		return new BoardWriteResult(WriteStatus.SUCCESS, trelloCardId, trelloCardUrl, null);
	}

	public static BoardWriteResult stub(String stubCardId, String stubCardUrl) {
		return new BoardWriteResult(WriteStatus.STUB, stubCardId, stubCardUrl, null);
	}

	public static BoardWriteResult failure(String errorDetails) {
		return new BoardWriteResult(WriteStatus.FAILED, null, null, errorDetails);
	}
}