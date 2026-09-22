package com.relaya.demo.analysis.adapter.in.web;

import com.relaya.demo.analysis.adapter.in.sse.AnalysisSseBroadcaster;
import com.relaya.demo.config.security.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/intakes")
public class AnalysisSseController {

	private static final Long SSE_TIMEOUT_MS = 300_000L;

	private final AnalysisSseBroadcaster sseBroadcaster;

	public AnalysisSseController(AnalysisSseBroadcaster sseBroadcaster) {
		this.sseBroadcaster = Objects.requireNonNull(sseBroadcaster, "sseBroadcaster must not be null");
	}

	@GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribeToAnalysisEvents(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable UUID id
	) {
		SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
		sseBroadcaster.registerEmitter(id, emitter);
		return emitter;
	}
}