package com.relaya.demo.review.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relaya.demo.analysis.domain.AnalysisDraft;
import com.relaya.demo.analysis.domain.SourceExcerpt;
import com.relaya.demo.analysis.domain.TaskProposal;
import com.relaya.demo.analysis.port.out.AnalysisDraftRepositoryPort;
import com.relaya.demo.config.security.JwtService;
import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.domain.IntakeVersion;
import com.relaya.demo.intake.domain.ServiceType;
import com.relaya.demo.intake.port.out.IntakeRepositoryPort;
import com.relaya.demo.intake.port.out.IntakeVersionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
class ReviewAndBoardWebIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private IntakeRepositoryPort intakeRepo;

	@Autowired
	private IntakeVersionRepositoryPort versionRepo;

	@Autowired
	private AnalysisDraftRepositoryPort draftRepo;

	private MockMvc mockMvc;
	private final UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private final UUID reviewerId = UUID.fromString("00000000-0000-0000-0000-000000000011");
	private String accessToken;

	@BeforeEach
	void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();
		this.accessToken = jwtService.generateAccessToken(reviewerId, tenantId, "reviewer@relaya.demo", "ROLE_REVIEWER");
	}

	@Test
	@DisplayName("Review draft edit, approval, and board write workflow")
	void shouldExecuteReviewAndBoardWorkflow() throws Exception {
		Intake intake = Intake.createNew(tenantId, "Bakery Review Client", ServiceType.WEBSITE_DELIVERY);
		intake.markAnalysisPending();
		intake.markAnalysisReady();
		intakeRepo.save(intake);

		IntakeVersion version = IntakeVersion.createInitial(tenantId, intake.getId(), "Raw bakery description text");
		versionRepo.save(version);

		AnalysisDraft draft = AnalysisDraft.createNew(
				tenantId, intake.getId(), version.getId(),
				List.of(new SourceExcerpt("Online ordering", "online ordering")),
				List.of(new SourceExcerpt("Menu catalog", "menu catalog")),
				List.of(), "2 weeks", "$2500",
				List.of(), List.of(new TaskProposal("Setup catalog", 2))
		);
		draftRepo.save(draft);

		String draftId = draft.getId().toString();

		// 1. GET draft
		mockMvc.perform(get("/api/v1/drafts/" + draftId)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.draftId").value(draftId))
				.andExpect(jsonPath("$.contentHash").value(draft.getContentHash().value()))
				.andExpect(jsonPath("$.isApproved").value(false));

		// 2. PUT draft (update)
		ReviewDto.UpdateDraftRequest updateReq = new ReviewDto.UpdateDraftRequest(
				List.of(new SourceExcerpt("Online ordering v2", "online ordering")),
				List.of(new SourceExcerpt("Menu catalog v2", "menu catalog")),
				List.of(), "3 weeks", "$3000",
				List.of("Delivery zones"), List.of(new TaskProposal("Setup catalog", 3))
		);

		String updateRes = mockMvc.perform(put("/api/v1/drafts/" + draftId)
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReq)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.draftId").value(draftId))
				.andExpect(jsonPath("$.contentHash").isNotEmpty())
				.andReturn()
				.getResponse()
				.getContentAsString();

		ReviewDto.UpdateDraftResponse updated = objectMapper.readValue(updateRes, ReviewDto.UpdateDraftResponse.class);

		// 3. POST approve
		ReviewDto.ApproveDraftRequest approveReq = new ReviewDto.ApproveDraftRequest(updated.contentHash());
		String approveRes = mockMvc.perform(post("/api/v1/drafts/" + draftId + "/approve")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(approveReq)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.approvalId").isNotEmpty())
				.andExpect(jsonPath("$.contentHashAtApproval").value(updated.contentHash()))
				.andReturn()
				.getResponse()
				.getContentAsString();

		ReviewDto.ApproveDraftResponse approved = objectMapper.readValue(approveRes, ReviewDto.ApproveDraftResponse.class);
		String approvalId = approved.approvalId().toString();

		// 4. POST board write (STUB)
		mockMvc.perform(post("/api/v1/approvals/" + approvalId + "/write")
						.header("Authorization", "Bearer " + accessToken)
						.header("Idempotency-Key", UUID.randomUUID().toString()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("STUB"))
				.andExpect(jsonPath("$.destinationBoard").value("TRELLO"));

		// 5. POST board write duplicate (idempotent returns existing)
		mockMvc.perform(post("/api/v1/approvals/" + approvalId + "/write")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("STUB"));
	}
}