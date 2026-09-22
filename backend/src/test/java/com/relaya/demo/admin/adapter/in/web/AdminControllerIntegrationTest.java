package com.relaya.demo.admin.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relaya.demo.config.security.JwtService;
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
class AdminControllerIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtService jwtService;

	private MockMvc mockMvc;
	private final UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private final UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000010");
	private final UUID reviewerId = UUID.fromString("00000000-0000-0000-0000-000000000011");

	@BeforeEach
	void init() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();
	}

	@Test
	@DisplayName("Reviewer is forbidden from accessing /admin endpoints")
	void reviewerShouldBeForbiddenFromAdmin() throws Exception {
		String reviewerToken = jwtService.generateAccessToken(reviewerId, tenantId, "reviewer@relaya.demo", "ROLE_REVIEWER");

		mockMvc.perform(get("/api/v1/admin/usage")
						.header("Authorization", "Bearer " + reviewerToken))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("Admin can access usage and manage users")
	void adminCanAccessUsageAndManageUsers() throws Exception {
		String adminToken = jwtService.generateAccessToken(adminId, tenantId, "admin@relaya.demo", "ROLE_ADMIN");

		// 1. GET /admin/usage
		mockMvc.perform(get("/api/v1/admin/usage")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.billingCycle").isNotEmpty())
				.andExpect(jsonPath("$.budgetLimitUsd").value(5.00));

		// 2. GET /admin/writes/failed
		mockMvc.perform(get("/api/v1/admin/writes/failed")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());

		// 3. POST /admin/users (create new reviewer)
		String uniqueEmail = "reviewer-" + UUID.randomUUID().toString().substring(0, 8) + "@agency.com";
		AdminDto.CreateUserRequest createReq = new AdminDto.CreateUserRequest(uniqueEmail, "securepass123", "ROLE_REVIEWER");

		mockMvc.perform(post("/api/v1/admin/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReq)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(uniqueEmail))
				.andExpect(jsonPath("$.role").value("ROLE_REVIEWER"))
				.andExpect(jsonPath("$.active").value(true));

		// 4. GET /admin/users
		mockMvc.perform(get("/api/v1/admin/users")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}