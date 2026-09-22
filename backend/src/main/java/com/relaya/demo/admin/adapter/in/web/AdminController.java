package com.relaya.demo.admin.adapter.in.web;

import com.relaya.demo.admin.service.AdminService;
import com.relaya.demo.config.security.UserPrincipal;
import com.relaya.demo.user.UserJpaEntity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = Objects.requireNonNull(adminService, "adminService must not be null");
	}

	@GetMapping("/usage")
	public ResponseEntity<AdminService.UsageSummary> getUsageSummary(
			@AuthenticationPrincipal UserPrincipal principal
	) {
		return ResponseEntity.ok(adminService.getUsageSummary(principal.tenantId()));
	}

	@GetMapping("/writes/failed")
	public ResponseEntity<AdminDto.FailedWritesResponse> getFailedWrites(
			@AuthenticationPrincipal UserPrincipal principal
	) {
		return ResponseEntity.ok(new AdminDto.FailedWritesResponse(adminService.getFailedWrites(principal.tenantId())));
	}

	@GetMapping("/users")
	public ResponseEntity<List<AdminDto.UserSummaryResponse>> listUsers(
			@AuthenticationPrincipal UserPrincipal principal
	) {
		List<UserJpaEntity> users = adminService.listUsers(principal.tenantId());
		List<AdminDto.UserSummaryResponse> response = users.stream()
				.map(u -> new AdminDto.UserSummaryResponse(
						u.getId(), u.getTenantId(), u.getEmail(), u.getRole(), u.isActive(), u.getCreatedAt()
				))
				.toList();
		return ResponseEntity.ok(response);
	}

	@PostMapping("/users")
	public ResponseEntity<AdminDto.UserSummaryResponse> createUser(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody AdminDto.CreateUserRequest request
	) {
		UserJpaEntity created = adminService.createUser(
				principal.tenantId(), request.email(), request.password(), request.role()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(new AdminDto.UserSummaryResponse(
				created.getId(), created.getTenantId(), created.getEmail(),
				created.getRole(), created.isActive(), created.getCreatedAt()
		));
	}

	@PatchMapping("/users/{id}")
	public ResponseEntity<AdminDto.UserSummaryResponse> updateUserStatus(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id,
			@RequestBody AdminDto.UpdateUserStatusRequest request
	) {
		UserJpaEntity updated = adminService.setUserActiveStatus(principal.tenantId(), id, request.active());
		return ResponseEntity.ok(new AdminDto.UserSummaryResponse(
				updated.getId(), updated.getTenantId(), updated.getEmail(),
				updated.getRole(), updated.isActive(), updated.getCreatedAt()
		));
	}
}