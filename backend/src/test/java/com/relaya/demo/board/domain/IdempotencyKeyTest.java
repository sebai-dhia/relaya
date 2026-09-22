package com.relaya.demo.board.domain;

import com.relaya.demo.analysis.domain.ContentHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdempotencyKeyTest {

	@Test
	@DisplayName("Generates deterministic format: approvalId_hash")
	void shouldGenerateDeterministicKey() {
		UUID approvalId = UUID.fromString("00000000-0000-0000-0000-000000000099");
		ContentHash hash = ContentHash.compute(List.of(), List.of(), List.of(), List.of(), List.of(), "", "");

		IdempotencyKey key = IdempotencyKey.of(approvalId, hash);

		assertEquals(approvalId + "_" + hash.value(), key.value());
	}

	@Test
	@DisplayName("Rejects null or blank values")
	void shouldRejectInvalidValues() {
		assertThrows(NullPointerException.class, () -> IdempotencyKey.of(null, null));
		assertThrows(IllegalArgumentException.class, () -> new IdempotencyKey("   "));
	}
}