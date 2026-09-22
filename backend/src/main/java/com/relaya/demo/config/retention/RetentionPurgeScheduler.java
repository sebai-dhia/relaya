package com.relaya.demo.config.retention;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
public class RetentionPurgeScheduler {

	private static final Logger log = LoggerFactory.getLogger(RetentionPurgeScheduler.class);

	private final JdbcTemplate jdbcTemplate;
	private final int retentionDays;

	public RetentionPurgeScheduler(
			JdbcTemplate jdbcTemplate,
			@Value("${relaya.retention.days:90}") int retentionDays
	) {
		this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
		this.retentionDays = retentionDays;
	}

	@Scheduled(cron = "${relaya.retention.cron:0 0 2 * * ?}")
	@Transactional
	public void executeDailyPurge() {
		Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
		log.info("Starting retention purge job for records older than {} days (cutoff: {})", retentionDays, cutoff);

		// Intakes cascade deletion to versions, drafts, approvals, board_writes, audit_events.
		// usage_ledger rows are preserved with intake_id set to NULL by DB foreign key constraint.
		String sql = "DELETE FROM intakes WHERE created_at < ?";
		int deletedCount = jdbcTemplate.update(sql, cutoff);

		log.info("Retention purge completed. Purged {} expired intake records and associated entities.", deletedCount);
	}

	public int getRetentionDays() {
		return retentionDays;
	}
}