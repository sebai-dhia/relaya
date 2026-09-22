package com.relaya.demo.intake.port.in;

import com.relaya.demo.intake.domain.ServiceType;
import java.util.Objects;
import java.util.UUID;

public record SubmitIntakeCommand(
		UUID tenantId,
		String clientLabel,
		ServiceType serviceType,
		String rawText
) {
	public SubmitIntakeCommand {
		Objects.requireNonNull(tenantId, "tenantId must not be null");
		if (clientLabel == null || clientLabel.isBlank()) {
			throw new IllegalArgumentException("clientLabel must not be empty");
		}
		if (serviceType == null) {
			serviceType = ServiceType.WEBSITE_DELIVERY;
		}
		if (rawText == null || rawText.isBlank()) {
			throw new IllegalArgumentException("rawText must not be empty");
		}
	}
}