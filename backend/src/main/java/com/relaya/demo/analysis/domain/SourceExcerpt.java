package com.relaya.demo.analysis.domain;

import java.util.Objects;

public record SourceExcerpt(
		String text,
		String sourceExcerpt
) implements Comparable<SourceExcerpt> {

	public SourceExcerpt {
		text = (text == null) ? "" : text.trim();
		sourceExcerpt = (sourceExcerpt == null) ? "" : sourceExcerpt.trim();
	}

	@Override
	public int compareTo(SourceExcerpt other) {
		if (other == null) {
			return 1;
		}
		int cmp = this.text.compareTo(other.text);
		if (cmp != 0) {
			return cmp;
		}
		return this.sourceExcerpt.compareTo(other.sourceExcerpt);
	}
}