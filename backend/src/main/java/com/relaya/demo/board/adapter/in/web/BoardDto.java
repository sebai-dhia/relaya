package com.relaya.demo.board.adapter.in.web;

import java.util.UUID;

public class BoardDto {

	public record BoardWriteResponse(
			UUID boardWriteId,
			String status,
			String destinationBoard,
			String trelloCardId,
			String trelloCardUrl,
			String message
	) {}
}