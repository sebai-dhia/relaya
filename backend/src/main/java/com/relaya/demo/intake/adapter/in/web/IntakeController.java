package com.relaya.demo.intake.adapter.in.web;

import com.relaya.demo.audit.domain.AuditEvent;
import com.relaya.demo.config.security.UserPrincipal;
import com.relaya.demo.intake.adapter.out.persistence.IntakeJpaEntity;
import com.relaya.demo.intake.domain.Intake;
import com.relaya.demo.intake.port.in.SubmitIntakeCommand;
import com.relaya.demo.intake.service.GetIntakeService;
import com.relaya.demo.intake.service.SubmitIntakeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.relaya.demo.intake.port.in.DeleteIntakeUseCase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/intakes")
public class IntakeController {

	private final SubmitIntakeService submitIntakeService;
	private final GetIntakeService getIntakeService;
	private final DeleteIntakeUseCase deleteIntakeUseCase;

	public IntakeController(
			SubmitIntakeService submitIntakeService,
			GetIntakeService getIntakeService,
			DeleteIntakeUseCase deleteIntakeUseCase
	) {
		this.submitIntakeService = Objects.requireNonNull(submitIntakeService, "submitIntakeService must not be null");
		this.getIntakeService = Objects.requireNonNull(getIntakeService, "getIntakeService must not be null");
		this.deleteIntakeUseCase = Objects.requireNonNull(deleteIntakeUseCase, "deleteIntakeUseCase must not be null");
	}


	@PostMapping
	public ResponseEntity<IntakeDto.CreateIntakeResponse> createIntake(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody IntakeDto.CreateIntakeRequest request
	) {
		SubmitIntakeCommand command = new SubmitIntakeCommand(
				principal.tenantId(),
				request.clientLabel(),
				request.serviceType(),
				request.rawText()
		);
		Intake intake = submitIntakeService.submitIntake(command);
		return ResponseEntity.status(HttpStatus.CREATED).body(new IntakeDto.CreateIntakeResponse(
				intake.getId(),
				1,
				intake.getStatus().name(),
				intake.getCreatedAt()
		));
	}

	@GetMapping
	public ResponseEntity<IntakeDto.PaginatedIntakesResponse> listIntakes(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String status
	) {
		Page<IntakeJpaEntity> resultPage = getIntakeService.listIntakes(principal.tenantId(), status, page, size);
		List<IntakeDto.IntakeSummary> content = resultPage.getContent().stream()
				.map(e -> new IntakeDto.IntakeSummary(e.getId(), e.getClientLabel(), e.getStatus().name(), e.getCreatedAt()))
				.toList();
		return ResponseEntity.ok(new IntakeDto.PaginatedIntakesResponse(
				content,
				resultPage.getNumber(),
				resultPage.getSize(),
				resultPage.getTotalElements()
		));
	}

	@GetMapping("/{id}")
	public ResponseEntity<GetIntakeService.IntakeDetails> getIntake(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id
	) {
		GetIntakeService.IntakeDetails details = getIntakeService.getIntakeDetails(principal.tenantId(), id);
		return ResponseEntity.ok(details);
	}

	@GetMapping("/{id}/audit")
	public ResponseEntity<IntakeDto.IntakeAuditTrailResponse> getAuditTrail(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id
	) {
		List<AuditEvent> events = getIntakeService.getIntakeAuditTrail(principal.tenantId(), id);
		List<IntakeDto.AuditEventItem> items = events.stream()
				.map(e -> new IntakeDto.AuditEventItem(
						e.eventType().name(),
						e.occurredAt(),
						e.userId(),
						e.payloadJson()
				))
				.toList();
		return ResponseEntity.ok(new IntakeDto.IntakeAuditTrailResponse(id, items));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteIntake(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id
	) {
		deleteIntakeUseCase.deleteIntake(principal.tenantId(), id);
		return ResponseEntity.noContent().build();
	}
}