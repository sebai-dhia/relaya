package com.relaya.demo.analysis.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

public record ContentHash(String value) {

	public ContentHash {
		Objects.requireNonNull(value, "value must not be null");
		if (value.length() != 64) {
			throw new IllegalArgumentException("ContentHash must be a 64-character hex string");
		}
	}

	public static ContentHash compute(
			List<SourceExcerpt> objectives,
			List<SourceExcerpt> deliverables,
			List<SourceExcerpt> constraints,
			List<String> unknowns,
			List<TaskProposal> proposedTasks,
			String timelineNotes,
			String budgetNotes
	) {
		String canonicalJson = buildCanonicalJson(
				objectives, deliverables, constraints,
				unknowns, proposedTasks, timelineNotes, budgetNotes
		);
		return new ContentHash(sha256Hex(canonicalJson));
	}

	public static String buildCanonicalJson(
			List<SourceExcerpt> objectives,
			List<SourceExcerpt> deliverables,
			List<SourceExcerpt> constraints,
			List<String> unknowns,
			List<TaskProposal> proposedTasks,
			String timelineNotes,
			String budgetNotes
	) {
		List<SourceExcerpt> sortedObjectives = sortExcerpts(objectives);
		List<SourceExcerpt> sortedDeliverables = sortExcerpts(deliverables);
		List<SourceExcerpt> sortedConstraints = sortExcerpts(constraints);
		List<String> sortedUnknowns = sortStrings(unknowns);
		List<TaskProposal> sortedTasks = sortTasks(proposedTasks);

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"budgetNotes\":\"").append(escape(budgetNotes)).append("\",");
		sb.append("\"constraints\":").append(renderExcerptsJson(sortedConstraints)).append(",");
		sb.append("\"deliverables\":").append(renderExcerptsJson(sortedDeliverables)).append(",");
		sb.append("\"objectives\":").append(renderExcerptsJson(sortedObjectives)).append(",");
		sb.append("\"proposedTasks\":").append(renderTasksJson(sortedTasks)).append(",");
		sb.append("\"timelineNotes\":\"").append(escape(timelineNotes)).append("\",");
		sb.append("\"unknowns\":").append(renderStringsJson(sortedUnknowns));
		sb.append("}");
		return sb.toString();
	}

	private static List<SourceExcerpt> sortExcerpts(List<SourceExcerpt> input) {
		if (input == null || input.isEmpty()) {
			return List.of();
		}
		List<SourceExcerpt> copy = new ArrayList<>(input);
		Collections.sort(copy);
		return copy;
	}

	private static List<String> sortStrings(List<String> input) {
		if (input == null || input.isEmpty()) {
			return List.of();
		}
		List<String> normalized = new ArrayList<>();
		for (String s : input) {
			normalized.add(s == null ? "" : s.trim());
		}
		Collections.sort(normalized);
		return normalized;
	}

	private static List<TaskProposal> sortTasks(List<TaskProposal> input) {
		if (input == null || input.isEmpty()) {
			return List.of();
		}
		List<TaskProposal> copy = new ArrayList<>(input);
		Collections.sort(copy);
		return copy;
	}

	private static String renderExcerptsJson(List<SourceExcerpt> list) {
		if (list.isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			SourceExcerpt item = list.get(i);
			sb.append("{\"sourceExcerpt\":\"").append(escape(item.sourceExcerpt()))
					.append("\",\"text\":\"").append(escape(item.text())).append("\"}");
		}
		sb.append("]");
		return sb.toString();
	}

	private static String renderTasksJson(List<TaskProposal> list) {
		if (list.isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			TaskProposal task = list.get(i);
			sb.append("{\"estimateDays\":").append(task.estimateDays())
					.append(",\"title\":\"").append(escape(task.title())).append("\"}");
		}
		sb.append("]");
		return sb.toString();
	}

	private static String renderStringsJson(List<String> list) {
		if (list.isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append("\"").append(escape(list.get(i))).append("\"");
		}
		sb.append("]");
		return sb.toString();
	}

	private static String escape(String raw) {
		if (raw == null) {
			return "";
		}
		StringBuilder out = new StringBuilder();
		for (char c : raw.toCharArray()) {
			switch (c) {
				case '"' -> out.append("\\\"");
				case '\\' -> out.append("\\\\");
				case '\b' -> out.append("\\b");
				case '\f' -> out.append("\\f");
				case '\n' -> out.append("\\n");
				case '\r' -> out.append("\\r");
				case '\t' -> out.append("\\t");
				default -> {
					if (c < 32) {
						out.append(String.format("\\u%04x", (int) c));
					} else {
						out.append(c);
					}
				}
			}
		}
		return out.toString();
	}

	private static String sha256Hex(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 digest algorithm unavailable", e);
		}
	}
}