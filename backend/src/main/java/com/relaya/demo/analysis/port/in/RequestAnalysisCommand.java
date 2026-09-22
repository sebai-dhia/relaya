package com.relaya.demo.analysis.port.in;

import java.util.Objects;
import java.util.UUID;

public record RequestAnalysisCommand(
		UUID tenantId,
		UUID intakeId
) {
	public RequestAnalysisCommand {
		Objects.requireNonNull(tenantId, "tenantId must not be null");
		Objects.requireNonNull(intakeId, "intakeId must not be null");
	}
}