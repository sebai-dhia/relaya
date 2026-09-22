package com.relaya.demo.config.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTH_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String SSE_TOKEN_PARAM = "sseToken";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = Objects.requireNonNull(jwtService, "jwtService must not be null");
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String token = extractToken(request);

		if (token != null && jwtService.isTokenValid(token)) {
			Claims claims = jwtService.parseClaims(token);
			String scope = claims.get(JwtService.SCOPE_CLAIM, String.class);
			boolean isSseRequest = isSseEndpoint(request);

			if (JwtService.SCOPE_SSE_ONLY.equals(scope) && !isSseRequest) {
				filterChain.doFilter(request, response);
				return;
			}

			UUID userId = UUID.fromString(claims.getSubject());
			UUID tenantId = UUID.fromString(claims.get(JwtService.TENANT_ID_CLAIM, String.class));
			String email = claims.get(JwtService.EMAIL_CLAIM, String.class);
			String role = claims.get(JwtService.ROLE_CLAIM, String.class);

			UserPrincipal principal = new UserPrincipal(userId, tenantId, email, "", role, true);
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
					principal,
					null,
					principal.getAuthorities()
			);
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);
		}

		filterChain.doFilter(request, response);
	}

	private String extractToken(HttpServletRequest request) {
		String header = request.getHeader(AUTH_HEADER);
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			return header.substring(BEARER_PREFIX.length()).trim();
		}
		if (isSseEndpoint(request)) {
			String param = request.getParameter(SSE_TOKEN_PARAM);
			if (param != null && !param.isBlank()) {
				return param.trim();
			}
		}
		return null;
	}

	private boolean isSseEndpoint(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri.contains("/intakes/") && uri.endsWith("/events");
	}
}