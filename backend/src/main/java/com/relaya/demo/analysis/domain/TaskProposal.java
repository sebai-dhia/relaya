package com.relaya.demo.analysis.domain;

public record TaskProposal(
		String title,
		int estimateDays
) implements Comparable<TaskProposal> {

	public TaskProposal {
		title = (title == null) ? "" : title.trim();
		if (estimateDays < 0) {
			estimateDays = 0;
		}
	}

	@Override
	public int compareTo(TaskProposal other) {
		if (other == null) {
			return 1;
		}
		int cmp = this.title.compareTo(other.title);
		if (cmp != 0) {
			return cmp;
		}
		return Integer.compare(this.estimateDays, other.estimateDays);
	}
}