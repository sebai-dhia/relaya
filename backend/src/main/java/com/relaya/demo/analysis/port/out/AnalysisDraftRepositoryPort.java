package com.relaya.demo.analysis.port.out;

import com.relaya.demo.analysis.domain.AnalysisDraft;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisDraftRepositoryPort {

	AnalysisDraft save(AnalysisDraft draft);

	Optional<AnalysisDraft> findById(UUID tenantId, UUID draftId);

	Optional<AnalysisDraft> findByIntakeId(UUID tenantId, UUID intakeId);

	Optional<AnalysisDraft> findByIntakeVersionId(UUID tenantId, UUID intakeVersionId);
}