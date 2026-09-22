package com.relaya.demo.board.port.in;

import com.relaya.demo.board.domain.BoardWrite;

public interface TriggerBoardWriteUseCase {

	BoardWrite triggerBoardWrite(TriggerBoardWriteCommand command);
}