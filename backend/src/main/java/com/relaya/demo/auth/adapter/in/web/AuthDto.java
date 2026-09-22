package com.relaya.demo.auth.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class AuthDto {

	public record LoginRequest(
			@NotBlank @Email String email,
			@NotBlank String password
	) {}

	public record RefreshRequest(
			@NotBlank String refreshToken
	) {}

	public record UserProfileResponse(
			UUID id,
			String email,
			String role,
			UUID tenantId
	) {}

	public record LoginResponse(
			String accessToken,
			String refreshToken,
			long expiresInSeconds,
			UserProfileResponse user
	) {}

	public record RefreshResponse(
			String accessToken,
			long expiresInSeconds
	) {}

	public record SseTokenResponse(
			String sseToken,
			long expiresInSeconds
	) {}
}