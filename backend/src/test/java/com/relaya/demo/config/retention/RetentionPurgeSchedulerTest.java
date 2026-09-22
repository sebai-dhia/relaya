package com.relaya.demo.config.retention;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetentionPurgeSchedulerTest {

	private final JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
	private final RetentionPurgeScheduler scheduler = new RetentionPurgeScheduler(jdbcTemplate, 90);

	@Test
	@DisplayName("Executes daily purge query using cutoff timestamp")
	void shouldExecuteDailyPurge() {
		when(jdbcTemplate.update(eq("DELETE FROM intakes WHERE created_at < ?"), any(Instant.class)))
				.thenReturn(5);

		scheduler.executeDailyPurge();

		verify(jdbcTemplate).update(eq("DELETE FROM intakes WHERE created_at < ?"), any(Instant.class));
		assertEquals(90, scheduler.getRetentionDays());
	}
}