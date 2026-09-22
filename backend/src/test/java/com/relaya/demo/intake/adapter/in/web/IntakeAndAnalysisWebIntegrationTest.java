package com.relaya.demo.intake.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relaya.demo.config.security.JwtService;
import com.relaya.demo.intake.domain.ServiceType;
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

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
class IntakeAndAnalysisWebIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtService jwtService;

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
	@DisplayName("Intake and Analysis workflow via REST and SSE endpoints")
	void shouldExecuteIntakeAndAnalysisWorkflow() throws Exception {
		// 1. Submit Intake
		IntakeDto.CreateIntakeRequest createRequest = new IntakeDto.CreateIntakeRequest(
				"Acme Bakery",
				ServiceType.WEBSITE_DELIVERY,
				"Need a 5-page responsive site with an online menu and delivery schedule before Christmas."
		);

		String createResponse = mockMvc.perform(post("/api/v1/intakes")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createRequest)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.intakeId").isNotEmpty())
				.andExpect(jsonPath("$.version").value(1))
				.andExpect(jsonPath("$.status").value("INTAKE_SUBMITTED"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		IntakeDto.CreateIntakeResponse created = objectMapper.readValue(createResponse, IntakeDto.CreateIntakeResponse.class);
		String intakeId = created.intakeId().toString();

		// 2. List Intakes
		mockMvc.perform(get("/api/v1/intakes")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());

		// 3. Get Intake Details
		mockMvc.perform(get("/api/v1/intakes/" + intakeId)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.intakeId").value(intakeId))
				.andExpect(jsonPath("$.clientLabel").value("Acme Bakery"));

		// 4. Get Audit Trail
		mockMvc.perform(get("/api/v1/intakes/" + intakeId + "/audit")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.intakeId").value(intakeId))
				.andExpect(jsonPath("$.events[0].eventType").value("INTAKE_SUBMITTED"));

		// 5. Trigger Analysis (202 Accepted)
		mockMvc.perform(post("/api/v1/intakes/" + intakeId + "/analyze")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.status").value("ANALYSIS_PENDING"));

		// 6. Connect to SSE with sseToken
		String sseToken = jwtService.generateSseToken(reviewerId, tenantId, "reviewer@relaya.demo", "ROLE_REVIEWER");

		mockMvc.perform(get("/api/v1/intakes/" + intakeId + "/events")
						.param("sseToken", sseToken))
				.andExpect(status().isOk());
	}
}