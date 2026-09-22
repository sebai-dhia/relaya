package com.relaya.demo.board.adapter.out.trello;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.domain.SourceExcerpt;
import com.relaya.demo.analysis.domain.TaskProposal;
import com.relaya.demo.board.domain.IdempotencyKey;
import com.relaya.demo.board.domain.WriteStatus;
import com.relaya.demo.board.port.out.BoardWriteContext;
import com.relaya.demo.board.port.out.BoardWriteResult;
import com.relaya.demo.tenant.port.out.TenantConfigPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class StubBoardConnectorTest {

	private final TenantConfigPort tenantConfigPort = Mockito.mock(TenantConfigPort.class);
	private final StubBoardConnector connector = new StubBoardConnector(tenantConfigPort);

	@Test
	@DisplayName("Returns STUB result and generates simulated card URLs")
	void shouldReturnStubStatus() {
		UUID tenantId = UUID.randomUUID();
		UUID approvalId = UUID.randomUUID();
		when(tenantConfigPort.findConfigValue(tenantId, "TRELLO_BOARD_ID")).thenReturn(Optional.of("STUB"));

		AnalysisDraft draft = AnalysisDraft.createNew(
				tenantId, UUID.randomUUID(), UUID.randomUUID(),
				List.of(new SourceExcerpt("Build site", "brief")),
				List.of(new SourceExcerpt("Landing page", "brief")),
				List.of(),
				"2 weeks",
				"$3,000",
				List.of(),
				List.of(new TaskProposal("Setup repo", 1))
		);

		BoardWriteContext context = new BoardWriteContext(
				tenantId,
				approvalId,
				"Acme Corp",
				draft,
				IdempotencyKey.of(approvalId, draft.getContentHash())
		);

		BoardWriteResult result = connector.writeCard(context);

		assertEquals(WriteStatus.STUB, result.status());
		assertNotNull(result.trelloCardId());
		assertTrue(result.trelloCardId().startsWith("stub-card-"));
		assertNotNull(result.trelloCardUrl());
		assertTrue(result.trelloCardUrl().contains("https://trello.com/c/stub-card-"));
	}
}