package com.relaya.demo.review.port.in;

import com.relaya.demo.review.domain.Approval;

public interface ApproveDraftUseCase {

	Approval approveDraft(ApproveDraftCommand command);
}