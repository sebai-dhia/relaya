package com.relaya.demo.analysis.adapter.in.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AnalysisSseBroadcaster {

	private static final Logger log = LoggerFactory.getLogger(AnalysisSseBroadcaster.class);

	private final Map<UUID, List<SseEmitter>> emittersByIntake = new ConcurrentHashMap<>();

	public void registerEmitter(UUID intakeId, SseEmitter emitter) {
		emittersByIntake.computeIfAbsent(intakeId, k -> new CopyOnWriteArrayList<>()).add(emitter);

		emitter.onCompletion(() -> removeEmitter(intakeId, emitter));
		emitter.onTimeout(() -> removeEmitter(intakeId, emitter));
		emitter.onError(e -> removeEmitter(intakeId, emitter));
	}

	public void broadcastStarted(UUID intakeId) {
		String payload = "{\"status\":\"ANALYSIS_PENDING\"}";
		sendEvent(intakeId, "ANALYSIS_STARTED", payload, false);
	}

	public void broadcastCompleted(UUID intakeId, UUID draftId) {
		String payload = "{\"status\":\"ANALYSIS_READY\",\"draftId\":\"" + draftId + "\"}";
		sendEvent(intakeId, "ANALYSIS_COMPLETED", payload, true);
	}

	public void broadcastFailed(UUID intakeId, String error) {
		String safeError = error != null ? error.replace("\"", "\\\"") : "Analysis failed";
		String payload = "{\"status\":\"ANALYSIS_FAILED\",\"error\":\"" + safeError + "\"}";
		sendEvent(intakeId, "ANALYSIS_FAILED", payload, true);
	}

	private void sendEvent(UUID intakeId, String eventName, String data, boolean complete) {
		List<SseEmitter> emitters = emittersByIntake.get(intakeId);
		if (emitters == null || emitters.isEmpty()) {
			return;
		}

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event().name(eventName).data(data));
				if (complete) {
					emitter.complete();
				}
			} catch (IOException e) {
				log.warn("Failed to send SSE event {} to emitter for intake {}: {}", eventName, intakeId, e.getMessage());
				removeEmitter(intakeId, emitter);
			}
		}

		if (complete) {
			emittersByIntake.remove(intakeId);
		}
	}

	private void removeEmitter(UUID intakeId, SseEmitter emitter) {
		List<SseEmitter> emitters = emittersByIntake.get(intakeId);
		if (emitters != null) {
			emitters.remove(emitter);
			if (emitters.isEmpty()) {
				emittersByIntake.remove(intakeId);
			}
		}
	}
}