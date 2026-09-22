package com.relaya.demo.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

	private static final int MAX_LOGIN_ATTEMPTS = 5;
	private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

	private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().endsWith("/api/v1/auth/login")
				|| !"POST".equalsIgnoreCase(request.getMethod());
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String clientIp = resolveClientIp(request);
		Bucket bucket = ipBuckets.computeIfAbsent(clientIp, k -> createNewBucket());

		if (bucket.tryConsume(1)) {
			filterChain.doFilter(request, response);
			return;
		}

		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		String errorBody = """
				{
				  "error": "RATE_LIMIT_EXCEEDED",
				  "message": "Too many login attempts. Please try again later.",
				  "timestamp": "%s"
				}""".formatted(Instant.now());
		response.getWriter().write(errorBody);
	}

	public void reset() {
		ipBuckets.clear();
	}

	private Bucket createNewBucket() {
		Bandwidth limit = Bandwidth.builder()
				.capacity(MAX_LOGIN_ATTEMPTS)
				.refillGreedy(MAX_LOGIN_ATTEMPTS, REFILL_DURATION)
				.build();
		return Bucket.builder().addLimit(limit).build();
	}

	private String resolveClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}