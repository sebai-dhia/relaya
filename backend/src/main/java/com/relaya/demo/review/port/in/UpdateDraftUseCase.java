package com.relaya.demo.review.port.in;

import com.relaya.demo.analysis.domain.AnalysisDraft;

public interface UpdateDraftUseCase {

	AnalysisDraft updateDraft(UpdateDraftCommand command);
}