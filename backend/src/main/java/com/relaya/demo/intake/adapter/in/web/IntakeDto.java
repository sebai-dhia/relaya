package com.relaya.demo.intake.adapter.in.web;

import com.relaya.demo.intake.domain.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class IntakeDto {

	public record CreateIntakeRequest(
			@NotBlank @Size(max = 150) String clientLabel,
			ServiceType serviceType,
			@NotBlank String rawText
	) {}

	public record CreateIntakeResponse(
			UUID intakeId,
			int version,
			String status,
			Instant createdAt
	) {}

	public record IntakeSummary(
			UUID intakeId,
			String clientLabel,
			String status,
			Instant createdAt
	) {}

	public record PaginatedIntakesResponse(
			List<IntakeSummary> content,
			int page,
			int size,
			long totalElements
	) {}

	public record IntakeAuditTrailResponse(
			UUID intakeId,
			List<AuditEventItem> events
	) {}

	public record AuditEventItem(
			String eventType,
			Instant occurredAt,
			UUID userId,
			Object payload
	) {}
}