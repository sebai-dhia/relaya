package com.relaya.demo.analysis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentHashTest {

	@Test
	@DisplayName("Canonical JSON sorts keys alphabetically")
	void shouldSortKeysAlphabetically() {
		String json = ContentHash.buildCanonicalJson(
				List.of(new SourceExcerpt("Launch site", "We need a website")),
				List.of(new SourceExcerpt("Landing page", "Deliver a responsive landing page")),
				List.of(new SourceExcerpt("No WordPress", "Do not use WordPress")),
				List.of("Hosting provider?"),
				List.of(new TaskProposal("Scaffold UI", 3)),
				"Q4 launch",
				"$5,000"
		);

		assertTrue(json.startsWith("{\"budgetNotes\":\"$5,000\",\"constraints\":"));
		assertTrue(json.contains("\"deliverables\":"));
		assertTrue(json.contains("\"objectives\":"));
		assertTrue(json.contains("\"proposedTasks\":"));
		assertTrue(json.contains("\"timelineNotes\":\"Q4 launch\""));
		assertTrue(json.endsWith("\"unknowns\":[\"Hosting provider?\"]}"));
	}

	@Test
	@DisplayName("Arrays are sorted regardless of input order")
	void shouldSortArrayElementsConsistently() {
		ContentHash hash1 = ContentHash.compute(
				List.of(
						new SourceExcerpt("Beta goal", "excerpt B"),
						new SourceExcerpt("Alpha goal", "excerpt A")
				),
				List.of(),
				List.of(),
				List.of("Zebra question", "Apple question"),
				List.of(
						new TaskProposal("Task B", 2),
						new TaskProposal("Task A", 1)
				),
				"",
				""
		);

		ContentHash hash2 = ContentHash.compute(
				List.of(
						new SourceExcerpt("Alpha goal", "excerpt A"),
						new SourceExcerpt("Beta goal", "excerpt B")
				),
				List.of(),
				List.of(),
				List.of("Apple question", "Zebra question"),
				List.of(
						new TaskProposal("Task A", 1),
						new TaskProposal("Task B", 2)
				),
				null,
				null
		);

		assertEquals(hash1.value(), hash2.value(), "Hash must match despite input array ordering");
		assertEquals(64, hash1.value().length(), "Hash must be 64-character hex");
	}

	@Test
	@DisplayName("Null and empty strings are treated identically")
	void shouldTreatNullAndEmptyStringsIdentically() {
		ContentHash hashWithNulls = ContentHash.compute(
				List.of(new SourceExcerpt("Goal", null)),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				null,
				null
		);

		ContentHash hashWithEmpties = ContentHash.compute(
				List.of(new SourceExcerpt("Goal", "")),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				"",
				""
		);

		assertEquals(hashWithNulls.value(), hashWithEmpties.value());
	}

	@Test
	@DisplayName("Mutating content produces different hash")
	void shouldProduceDifferentHashOnMutation() {
		ContentHash original = ContentHash.compute(
				List.of(new SourceExcerpt("Goal 1", "Excerpt 1")),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				"Timeline",
				"Budget"
		);

		ContentHash mutated = ContentHash.compute(
				List.of(new SourceExcerpt("Goal 1 edited", "Excerpt 1")),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				"Timeline",
				"Budget"
		);

		assertNotEquals(original.value(), mutated.value());
	}
}