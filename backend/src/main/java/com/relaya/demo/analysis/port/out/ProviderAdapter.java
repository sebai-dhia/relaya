package com.relaya.demo.analysis.port.out;

import java.util.UUID;

public interface ProviderAdapter {

	ProviderExtractionResult extractAnalysis(UUID tenantId, String rawText);
}