package com.relaya.demo.board.adapter.out.trello;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.board.domain.WriteStatus;
import com.relaya.demo.board.port.out.BoardConnector;
import com.relaya.demo.board.port.out.BoardWriteContext;
import com.relaya.demo.board.port.out.BoardWriteResult;
import com.relaya.demo.tenant.port.out.TenantConfigPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Primary
@Component
public class TrelloBoardConnector implements BoardConnector {

	private static final Logger log = LoggerFactory.getLogger(TrelloBoardConnector.class);
	private static final String TRELLO_BASE_URL = "https://api.trello.com/1";

	private final TenantConfigPort tenantConfigPort;
	private final StubBoardConnector stubBoardConnector;
	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final String envApiKey;
	private final String envToken;
	private final String envBoardId;

	public TrelloBoardConnector(
			TenantConfigPort tenantConfigPort,
			StubBoardConnector stubBoardConnector,
			@Value("${relaya.trello.api-key:}") String envApiKey,
			@Value("${relaya.trello.token:}") String envToken,
			@Value("${relaya.trello.board-id:}") String envBoardId,
			RestClient restClient
	) {
		this.tenantConfigPort = Objects.requireNonNull(tenantConfigPort, "tenantConfigPort must not be null");
		this.stubBoardConnector = Objects.requireNonNull(stubBoardConnector, "stubBoardConnector must not be null");
		this.envApiKey = envApiKey != null ? envApiKey.trim() : "";
		this.envToken = envToken != null ? envToken.trim() : "";
		this.envBoardId = envBoardId != null ? envBoardId.trim() : "";
		this.objectMapper = new ObjectMapper();
		this.restClient = Objects.requireNonNull(restClient, "restClient must not be null");
	}

	@Override
	public BoardWriteResult writeCard(BoardWriteContext context) {
		Objects.requireNonNull(context, "context must not be null");
		UUID tenantId = context.tenantId();

		String apiKey = resolveConfig(tenantId, "TRELLO_API_KEY", envApiKey);
		String token = resolveConfig(tenantId, "TRELLO_TOKEN", envToken);
		String listId = resolveConfig(tenantId, "TRELLO_BOARD_ID", envBoardId);

		if (isMissing(apiKey) || isMissing(token) || isMissing(listId) || "STUB".equalsIgnoreCase(listId)) {
			log.info("Trello credentials not configured for tenant {}. Delegating to STUB.", tenantId);
			return stubBoardConnector.writeCard(context);
		}

		try {
			return executeTrelloWrite(context, apiKey, token, listId);
		} catch (Exception e) {
			log.error("Failed to write Trello card for client '{}': {}", context.clientLabel(), e.getMessage(), e);
			return BoardWriteResult.failure("Trello API error: " + e.getMessage());
		}
	}

	private BoardWriteResult executeTrelloWrite(BoardWriteContext context, String key, String token, String listId) {
		String cardTitle = "[" + context.clientLabel() + "] Website Delivery Handoff";
		String description = buildCardDescription(context);

		Map<String, Object> payload = Map.of(
				"idList", listId,
				"name", cardTitle,
				"desc", description,
				"pos", "top"
		);

		String response = restClient.post()
				.uri(TRELLO_BASE_URL + "/cards?key={key}&token={token}", key, token)
				.contentType(MediaType.APPLICATION_JSON)
				.body(payload)
				.retrieve()
				.body(String.class);

		if (response == null || response.isBlank()) {
			return BoardWriteResult.failure("Empty response received from Trello API");
		}

		try {
			JsonNode root = objectMapper.readTree(response);
			String cardId = root.path("id").asText(null);
			String cardUrl = root.hasNonNull("shortUrl") ? root.path("shortUrl").asText() : root.path("url").asText();

			if (cardId == null || cardUrl == null) {
				return BoardWriteResult.failure("Invalid response from Trello: missing card id or URL");
			}

			log.info("Successfully created Trello card {} for client '{}': {}", cardId, context.clientLabel(), cardUrl);
			addChecklistsSilently(cardId, context.draft(), key, token);
			return BoardWriteResult.success(cardId, cardUrl);
		} catch (Exception e) {
			return BoardWriteResult.failure("Failed to parse Trello card response: " + e.getMessage());
		}
	}

	private void addChecklistsSilently(String cardId, AnalysisDraft draft, String key, String token) {
		if (draft.getDeliverables() == null || draft.getDeliverables().isEmpty()) {
			return;
		}
		try {
			String checklistRes = restClient.post()
					.uri(TRELLO_BASE_URL + "/checklists?idCard={cardId}&name={name}&key={key}&token={token}",
							cardId, "Deliverables Checklist", key, token)
					.retrieve()
					.body(String.class);
			if (checklistRes != null) {
				String checklistId = objectMapper.readTree(checklistRes).path("id").asText();
				for (var d : draft.getDeliverables()) {
					restClient.post()
							.uri(TRELLO_BASE_URL + "/checklists/{clId}/checkItems?name={name}&key={key}&token={token}",
									checklistId, d.text(), key, token)
							.retrieve()
							.toBodilessEntity();
				}
			}
		} catch (Exception e) {
			log.warn("Checklist addition to card {} encountered an issue: {}", cardId, e.getMessage());
		}
	}

	private String buildCardDescription(BoardWriteContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append("### Approved Website Delivery Scope\n\n");
		sb.append("**Client:** ").append(ctx.clientLabel()).append("\n");
		sb.append("**Approval ID:** `").append(ctx.approvalId()).append("`\n");
		sb.append("**Idempotency Key:** `").append(ctx.idempotencyKey().value()).append("`\n\n");
		if (ctx.draft().getTimelineNotes() != null && !ctx.draft().getTimelineNotes().isBlank()) {
			sb.append("**Timeline:** ").append(ctx.draft().getTimelineNotes()).append("\n");
		}
		if (ctx.draft().getBudgetNotes() != null && !ctx.draft().getBudgetNotes().isBlank()) {
			sb.append("**Budget:** ").append(ctx.draft().getBudgetNotes()).append("\n");
		}
		sb.append("\n#### Deliverables\n");
		ctx.draft().getDeliverables().forEach(d -> sb.append("- [ ] ").append(d.text()).append("\n"));
		if (ctx.draft().getProposedTasks() != null && !ctx.draft().getProposedTasks().isEmpty()) {
			sb.append("\n#### Proposed Tasks\n");
			ctx.draft().getProposedTasks().forEach(t -> sb.append("- [ ] ").append(t.title())
					.append(" (").append(t.estimateDays()).append("d)\n"));
		}
		return sb.toString();
	}

	private String resolveConfig(UUID tenantId, String key, String envFallback) {
		return tenantConfigPort.findConfigValue(tenantId, key)
				.filter(v -> !v.isBlank() && !"PLACEHOLDER".equalsIgnoreCase(v) && !"STUB".equalsIgnoreCase(v))
				.orElse(envFallback);
	}

	private boolean isMissing(String value) {
		return value == null || value.isBlank();
	}
}
