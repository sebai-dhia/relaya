package com.relaya.demo.board.port.out;

public interface BoardConnector {

	BoardWriteResult writeCard(BoardWriteContext context);
}