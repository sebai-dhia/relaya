package com.relaya.demo.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

	private static final Duration ACCESS_TOKEN_EXPIRY = Duration.ofMinutes(60);
	private static final Duration REFRESH_TOKEN_EXPIRY = Duration.ofDays(7);
	private static final Duration SSE_TOKEN_EXPIRY = Duration.ofMinutes(5);

	public static final String SCOPE_CLAIM = "scope";
	public static final String SCOPE_SSE_ONLY = "SSE_ONLY";
	public static final String ROLE_CLAIM = "role";
	public static final String TENANT_ID_CLAIM = "tenant_id";
	public static final String EMAIL_CLAIM = "email";

	private final SecretKey signingKey;

	public JwtService(
			@Value("${relaya.security.jwt.secret:default-relaya-secret-key-must-be-at-least-256-bits-long-for-hmac-sha-relaya}")
			String secretKeyString
	) {
		byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < 32) {
			byte[] padded = new byte[32];
			System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
			keyBytes = padded;
		}
		this.signingKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateAccessToken(UUID userId, UUID tenantId, String email, String role) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(userId.toString())
				.claims(Map.of(
						TENANT_ID_CLAIM, tenantId.toString(),
						EMAIL_CLAIM, email,
						ROLE_CLAIM, role
				))
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(ACCESS_TOKEN_EXPIRY)))
				.signWith(signingKey)
				.compact();
	}

	public String generateRefreshToken(UUID userId, UUID tenantId) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(userId.toString())
				.claims(Map.of(
						TENANT_ID_CLAIM, tenantId.toString(),
						"type", "REFRESH"
				))
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(REFRESH_TOKEN_EXPIRY)))
				.signWith(signingKey)
				.compact();
	}

	public String generateSseToken(UUID userId, UUID tenantId, String email, String role) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(userId.toString())
				.claims(Map.of(
						TENANT_ID_CLAIM, tenantId.toString(),
						EMAIL_CLAIM, email,
						ROLE_CLAIM, role,
						SCOPE_CLAIM, SCOPE_SSE_ONLY
				))
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(SSE_TOKEN_EXPIRY)))
				.signWith(signingKey)
				.compact();
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public boolean isTokenValid(String token) {
		try {
			Claims claims = parseClaims(token);
			return claims.getExpiration().after(new Date());
		} catch (Exception e) {
			return false;
		}
	}
}