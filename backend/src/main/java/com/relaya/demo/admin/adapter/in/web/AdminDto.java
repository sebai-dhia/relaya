package com.relaya.demo.admin.adapter.in.web;

import com.relaya.demo.admin.service.AdminService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AdminDto {

	public record FailedWritesResponse(
			List<AdminService.FailedWriteSummary> content
	) {}

	public record CreateUserRequest(
			@NotBlank @Email String email,
			@NotBlank String password,
			String role
	) {}

	public record UpdateUserStatusRequest(
			boolean active
	) {}

	public record UserSummaryResponse(
			UUID id,
			UUID tenantId,
			String email,
			String role,
			boolean active,
			Instant createdAt
	) {}
}