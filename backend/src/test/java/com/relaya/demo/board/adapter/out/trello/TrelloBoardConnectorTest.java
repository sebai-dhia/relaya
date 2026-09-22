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
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TrelloBoardConnectorTest {

	private final TenantConfigPort tenantConfigPort = Mockito.mock(TenantConfigPort.class);
	private final StubBoardConnector stubBoardConnector = new StubBoardConnector(tenantConfigPort);

	@Test
	@DisplayName("Delegates to STUB connector when credentials are unset or marked STUB")
	void shouldFallbackToStubWhenCredentialsMissing() {
		UUID tenantId = UUID.randomUUID();
		UUID approvalId = UUID.randomUUID();
		when(tenantConfigPort.findConfigValue(tenantId, "TRELLO_BOARD_ID")).thenReturn(Optional.of("STUB"));

		TrelloBoardConnector connector = new TrelloBoardConnector(
				tenantConfigPort, stubBoardConnector, "", "", "", RestClient.builder().build()
		);

		BoardWriteContext context = createContext(tenantId, approvalId);
		BoardWriteResult result = connector.writeCard(context);

		assertEquals(WriteStatus.STUB, result.status());
		assertNotNull(result.trelloCardId());
		assertTrue(result.trelloCardId().startsWith("stub-card-"));
	}

	@Test
	@DisplayName("Creates card and returns SUCCESS when Trello API responds successfully")
	void shouldCreateCardSuccessfully() {
		UUID tenantId = UUID.randomUUID();
		UUID approvalId = UUID.randomUUID();
		when(tenantConfigPort.findConfigValue(tenantId, "TRELLO_API_KEY")).thenReturn(Optional.of("test-key"));
		when(tenantConfigPort.findConfigValue(tenantId, "TRELLO_TOKEN")).thenReturn(Optional.of("test-token"));
		when(tenantConfigPort.findConfigValue(tenantId, "TRELLO_BOARD_ID")).thenReturn(Optional.of("test-list-id"));

		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

		String mockResponse = """
				{"id":"card-123","shortUrl":"https://trello.com/c/card-123","url":"https://trello.com/c/card-123/name"}
				""";

		server.expect(requestTo("https://api.trello.com/1/cards?key=test-key&token=test-token"))
				.andRespond(withSuccess(mockResponse, APPLICATION_JSON));

		TrelloBoardConnector connector = new TrelloBoardConnector(
				tenantConfigPort, stubBoardConnector, "", "", "", builder.build()
		);

		BoardWriteContext context = createContext(tenantId, approvalId);
		BoardWriteResult result = connector.writeCard(context);

		assertEquals(WriteStatus.SUCCESS, result.status());
		assertEquals("card-123", result.trelloCardId());
		assertEquals("https://trello.com/c/card-123", result.trelloCardUrl());
		server.verify();
	}

	private BoardWriteContext createContext(UUID tenantId, UUID approvalId) {
		AnalysisDraft draft = AnalysisDraft.createNew(
				tenantId, UUID.randomUUID(), UUID.randomUUID(),
				List.of(new SourceExcerpt("Build site", "brief")),
				List.of(),
				List.of(),
				"2 weeks",
				"$3,000",
				List.of(),
				List.of(new TaskProposal("Setup repo", 1))
		);

		return new BoardWriteContext(
				tenantId, approvalId, "Acme Corp", draft,
				IdempotencyKey.of(approvalId, draft.getContentHash())
		);
	}
}
