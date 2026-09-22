package com.relaya.demo.analysis.adapter.in.web;

import com.relaya.demo.analysis.port.in.RequestAnalysisCommand;
import com.relaya.demo.analysis.service.RequestAnalysisService;
import com.relaya.demo.config.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/intakes")
public class AnalysisController {

	private final RequestAnalysisService requestAnalysisService;

	public AnalysisController(RequestAnalysisService requestAnalysisService) {
		this.requestAnalysisService = Objects.requireNonNull(requestAnalysisService, "requestAnalysisService must not be null");
	}

	@PostMapping("/{id}/analyze")
	public ResponseEntity<Map<String, String>> triggerAnalysis(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id
	) {
		RequestAnalysisCommand command = new RequestAnalysisCommand(principal.tenantId(), id);
		requestAnalysisService.requestAnalysis(command);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
				"message", "Analysis initiated",
				"status", "ANALYSIS_PENDING"
		));
	}
}