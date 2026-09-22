package com.relaya.demo.review.domain;

import com.relaya.demo.analysis.domain.ContentHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalTest {

	@Test
	@DisplayName("New approval is valid and matches signed hash")
	void shouldCreateValidApprovalMatchingHash() {
		ContentHash hash = ContentHash.compute(List.of(), List.of(), List.of(), List.of(), List.of(), "", "");
		Approval approval = Approval.createNew(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), hash);

		assertTrue(approval.isValid());
		assertNull(approval.getInvalidatedAt());
		assertTrue(approval.matchesCurrentDraftHash(hash));
	}

	@Test
	@DisplayName("Approval fails match when draft hash is mutated")
	void shouldFailMatchWhenDraftHashChanges() {
		ContentHash hash1 = ContentHash.compute(List.of(), List.of(), List.of(), List.of(), List.of(), "v1", "");
		ContentHash hash2 = ContentHash.compute(List.of(), List.of(), List.of(), List.of(), List.of(), "v2", "");

		Approval approval = Approval.createNew(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), hash1);

		assertFalse(approval.matchesCurrentDraftHash(hash2));
	}

	@Test
	@DisplayName("Invalidating approval sets invalidatedAt and makes it invalid")
	void shouldInvalidateApproval() {
		ContentHash hash = ContentHash.compute(List.of(), List.of(), List.of(), List.of(), List.of(), "", "");
		Approval approval = Approval.createNew(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), hash);

		approval.invalidate();

		assertFalse(approval.isValid());
		assertNotNull(approval.getInvalidatedAt());
	}
}