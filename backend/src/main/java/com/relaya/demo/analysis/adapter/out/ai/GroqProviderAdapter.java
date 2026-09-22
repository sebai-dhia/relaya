package com.relaya.demo.analysis.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relaya.demo.analysis.domain.SourceExcerpt;
import com.relaya.demo.analysis.domain.TaskProposal;
import com.relaya.demo.analysis.port.out.ProviderAdapter;
import com.relaya.demo.analysis.port.out.ProviderExtractionResult;
import com.relaya.demo.tenant.port.out.TenantConfigPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class GroqProviderAdapter implements ProviderAdapter {

	private static final Logger log = LoggerFactory.getLogger(GroqProviderAdapter.class);
	private static final String DEFAULT_MODEL = "llama-3.1-8b-instant";
	private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

	private final TenantConfigPort tenantConfigPort;
	private final BudgetGuard budgetGuard;
	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final String envGroqApiKey;
	private final String envGroqModel;

	public GroqProviderAdapter(
			TenantConfigPort tenantConfigPort,
			BudgetGuard budgetGuard,
			@Value("${GROQ_API_KEY:}") String envGroqApiKey,
			@Value("${GROQ_MODEL:}") String envGroqModel,
			RestClient restClient
	) {
		this.tenantConfigPort = Objects.requireNonNull(tenantConfigPort, "tenantConfigPort must not be null");
		this.budgetGuard = Objects.requireNonNull(budgetGuard, "budgetGuard must not be null");
		this.envGroqApiKey = (envGroqApiKey == null) ? "" : envGroqApiKey.trim();
		this.envGroqModel = (envGroqModel == null) ? "" : envGroqModel.trim();
		this.objectMapper = new ObjectMapper();
		this.restClient = Objects.requireNonNull(restClient, "restClient must not be null");
	}

	@Override
	public ProviderExtractionResult extractAnalysis(UUID tenantId, String rawText) {
		Objects.requireNonNull(tenantId, "tenantId must not be null");
		Objects.requireNonNull(rawText, "rawText must not be null");

		int estimatedTokens = Math.max(rawText.length() / 4, 10);
		budgetGuard.checkGuards(tenantId, estimatedTokens);

		String apiKey = resolveApiKey(tenantId);
		String model = resolveModel(tenantId);

		if (apiKey.isBlank() || "PLACEHOLDER".equalsIgnoreCase(apiKey)) {
			log.info("Using synthetic extraction fallback for tenant {} (no live Groq API key)", tenantId);
			return generateSyntheticFallback(rawText, model, estimatedTokens);
		}

		try {
			return callGroqApi(rawText, model, apiKey, estimatedTokens);
		} catch (Exception e) {
			log.warn("Groq API call failed ({}). Falling back to synthetic extraction for demo resilience.", e.getMessage());
			return generateSyntheticFallback(rawText, model, estimatedTokens);
		}
	}

	private String resolveModel(UUID tenantId) {
		if (!envGroqModel.isBlank()) {
			return envGroqModel;
		}
		return tenantConfigPort.findConfigValue(tenantId, "GROQ_MODEL")
				.filter(m -> !m.isBlank() && !"llama-3.3-70b-versatile".equalsIgnoreCase(m))
				.orElse(DEFAULT_MODEL);
	}

	private String resolveApiKey(UUID tenantId) {
		return tenantConfigPort.findConfigValue(tenantId, "GROQ_API_KEY")
				.filter(k -> !k.isBlank() && !"PLACEHOLDER".equalsIgnoreCase(k))
				.orElse(envGroqApiKey);
	}

	private ProviderExtractionResult callGroqApi(String rawText, String model, String apiKey, int estimatedTokens) {
		String systemPrompt = """
				You are an expert website delivery analyst. Extract structured scope from the client brief.
				Respond ONLY with a valid JSON object with these exact keys:
				- "objectives": array of {"text": string, "sourceExcerpt": string}
				- "deliverables": array of {"text": string, "sourceExcerpt": string}
				- "constraints": array of {"text": string, "sourceExcerpt": string}
				- "timelineNotes": string
				- "budgetNotes": string
				- "unknowns": array of string (flag missing/unclear requirements)
				- "proposedTasks": array of {"title": string, "estimateDays": integer} (3 to 7 actionable tasks)
				Do not fabricate facts. Always link extracted items back to exact excerpts.
				""";

		Map<String, Object> requestBody = Map.of(
				"model", model,
				"messages", List.of(
						Map.of("role", "system", "content", systemPrompt),
						Map.of("role", "user", "content", rawText)
				),
				"response_format", Map.of("type", "json_object"),
				"temperature", 0.1
		);

		try {
			String responseStr = restClient.post()
					.uri(GROQ_API_URL)
					.header("Authorization", "Bearer " + apiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestBody)
					.retrieve()
					.body(String.class);

			JsonNode root = objectMapper.readTree(responseStr);
			String content = root.path("choices").get(0).path("message").path("content").asText();
			int promptTokens = root.path("usage").path("prompt_tokens").asInt(estimatedTokens);
			int completionTokens = root.path("usage").path("completion_tokens").asInt(200);

			JsonNode parsed = objectMapper.readTree(content);
			return parseExtractionNode(parsed, "GROQ", model, promptTokens, completionTokens);
		} catch (Exception e) {
			log.error("Groq API call failed: {}", e.getMessage(), e);
			throw new IllegalStateException("Groq API extraction failed: " + e.getMessage(), e);
		}
	}

	private ProviderExtractionResult parseExtractionNode(JsonNode n, String provider, String model, int promptTokens, int completionTokens) {
		List<SourceExcerpt> objectives = readExcerpts(n.path("objectives"));
		List<SourceExcerpt> deliverables = readExcerpts(n.path("deliverables"));
		List<SourceExcerpt> constraints = readExcerpts(n.path("constraints"));
		String timelineNotes = n.path("timelineNotes").asText("");
		String budgetNotes = n.path("budgetNotes").asText("");
		List<String> unknowns = readStrings(n.path("unknowns"));
		List<TaskProposal> tasks = readTasks(n.path("proposedTasks"));

		return new ProviderExtractionResult(
				objectives, deliverables, constraints,
				timelineNotes, budgetNotes, unknowns, tasks,
				provider, model, promptTokens, completionTokens
		);
	}

	private List<SourceExcerpt> readExcerpts(JsonNode arrayNode) {
		List<SourceExcerpt> list = new ArrayList<>();
		if (arrayNode.isArray()) {
			for (JsonNode item : arrayNode) {
				list.add(new SourceExcerpt(item.path("text").asText(""), item.path("sourceExcerpt").asText("")));
			}
		}
		return list;
	}

	private List<String> readStrings(JsonNode arrayNode) {
		List<String> list = new ArrayList<>();
		if (arrayNode.isArray()) {
			for (JsonNode item : arrayNode) {
				list.add(item.asText(""));
			}
		}
		return list;
	}

	private List<TaskProposal> readTasks(JsonNode arrayNode) {
		List<TaskProposal> list = new ArrayList<>();
		if (arrayNode.isArray()) {
			for (JsonNode item : arrayNode) {
				list.add(new TaskProposal(item.path("title").asText(""), item.path("estimateDays").asInt(1)));
			}
		}
		return list;
	}

	private ProviderExtractionResult generateSyntheticFallback(String rawText, String model, int estimatedPromptTokens) {
		String excerpt = rawText.length() > 60 ? rawText.substring(0, 60) + "..." : rawText;
		List<SourceExcerpt> objectives = List.of(new SourceExcerpt("Deliver modern responsive website", excerpt));
		List<SourceExcerpt> deliverables = List.of(new SourceExcerpt("Landing page with contact form", excerpt));
		List<SourceExcerpt> constraints = List.of(new SourceExcerpt("Standard responsive web constraints", excerpt));
		List<String> unknowns = List.of("Target hosting infrastructure to be confirmed");
		List<TaskProposal> proposedTasks = List.of(
				new TaskProposal("Architecture and project scaffold", 2),
				new TaskProposal("Core components and responsive layout", 4),
				new TaskProposal("Testing and staging deployment", 2)
		);
		return new ProviderExtractionResult(
				objectives, deliverables, constraints,
				"Estimated delivery timeline: 4 weeks",
				"Budget according to delivery scope",
				unknowns, proposedTasks,
				"GROQ_DEV_SYNTHETIC", model, estimatedPromptTokens, 150
		);
	}
}