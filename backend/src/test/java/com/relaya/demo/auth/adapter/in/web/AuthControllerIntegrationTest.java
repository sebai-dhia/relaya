package com.relaya.demo.auth.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relaya.demo.config.security.RateLimitingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
class AuthControllerIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RateLimitingFilter rateLimitingFilter;

	private MockMvc mockMvc;

	@BeforeEach
	void init() {
		rateLimitingFilter.reset();
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity())
				.build();
	}

	@Test
	@DisplayName("POST /api/v1/auth/login succeeds with seeded reviewer credentials")
	void shouldLoginSuccessfully() throws Exception {
		AuthDto.LoginRequest request = new AuthDto.LoginRequest("reviewer@relaya.demo", "changeit");

		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty())
				.andExpect(jsonPath("$.expiresInSeconds").value(3600))
				.andExpect(jsonPath("$.user.email").value("reviewer@relaya.demo"))
				.andExpect(jsonPath("$.user.role").value("ROLE_REVIEWER"))
				.andReturn();

		assertNotNull(result.getResponse().getContentAsString());
	}

	@Test
	@DisplayName("POST /api/v1/auth/login returns 401 on invalid password")
	void shouldFailLoginWithBadCredentials() throws Exception {
		AuthDto.LoginRequest request = new AuthDto.LoginRequest("reviewer@relaya.demo", "wrongpassword");

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
	}

	@Test
	@DisplayName("POST /api/v1/auth/refresh renews access token")
	void shouldRefreshToken() throws Exception {
		AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest("reviewer@relaya.demo", "changeit");
		String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		AuthDto.LoginResponse authData = objectMapper.readValue(loginResponse, AuthDto.LoginResponse.class);

		AuthDto.RefreshRequest refreshRequest = new AuthDto.RefreshRequest(authData.refreshToken());

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(refreshRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.expiresInSeconds").value(3600));
	}

	@Test
	@DisplayName("POST /api/v1/auth/sse-token generates 5-minute scoped token")
	void shouldGenerateSseToken() throws Exception {
		AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest("reviewer@relaya.demo", "changeit");
		String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		AuthDto.LoginResponse authData = objectMapper.readValue(loginResponse, AuthDto.LoginResponse.class);

		mockMvc.perform(post("/api/v1/auth/sse-token")
						.header("Authorization", "Bearer " + authData.accessToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sseToken").isNotEmpty())
				.andExpect(jsonPath("$.expiresInSeconds").value(300));
	}
}