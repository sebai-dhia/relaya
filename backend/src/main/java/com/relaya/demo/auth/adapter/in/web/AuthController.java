package com.relaya.demo.auth.adapter.in.web;

import com.relaya.demo.config.security.JwtService;
import com.relaya.demo.config.security.UserPrincipal;
import com.relaya.demo.user.SpringDataUserRepository;
import com.relaya.demo.user.UserJpaEntity;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final SpringDataUserRepository userRepo;

	public AuthController(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			SpringDataUserRepository userRepo
	) {
		this.authenticationManager = Objects.requireNonNull(authenticationManager, "authenticationManager must not be null");
		this.jwtService = Objects.requireNonNull(jwtService, "jwtService must not be null");
		this.userRepo = Objects.requireNonNull(userRepo, "userRepo must not be null");
	}

	@PostMapping("/login")
	public ResponseEntity<AuthDto.LoginResponse> login(@Valid @RequestBody AuthDto.LoginRequest request) {
		Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);
		UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

		String accessToken = jwtService.generateAccessToken(
				principal.userId(), principal.tenantId(), principal.email(), principal.role()
		);
		String refreshToken = jwtService.generateRefreshToken(principal.userId(), principal.tenantId());

		AuthDto.UserProfileResponse userProfile = new AuthDto.UserProfileResponse(
				principal.userId(), principal.email(), principal.role(), principal.tenantId()
		);

		return ResponseEntity.ok(new AuthDto.LoginResponse(accessToken, refreshToken, 3600L, userProfile));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthDto.RefreshResponse> refresh(@Valid @RequestBody AuthDto.RefreshRequest request) {
		if (!jwtService.isTokenValid(request.refreshToken())) {
			throw new BadCredentialsException("Refresh token is expired or invalid");
		}
		Claims claims = jwtService.parseClaims(request.refreshToken());
		UUID userId = UUID.fromString(claims.getSubject());
		UserJpaEntity user = userRepo.findById(userId)
				.filter(UserJpaEntity::isActive)
				.orElseThrow(() -> new BadCredentialsException("User inactive or not found"));

		String accessToken = jwtService.generateAccessToken(
				user.getId(), user.getTenantId(), user.getEmail(), user.getRole()
		);
		return ResponseEntity.ok(new AuthDto.RefreshResponse(accessToken, 3600L));
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@Valid @RequestBody AuthDto.RefreshRequest request) {
		// Stateless JWT: logout acknowledges token invalidation
	}

	@PostMapping("/sse-token")
	public ResponseEntity<AuthDto.SseTokenResponse> generateSseToken(@AuthenticationPrincipal UserPrincipal principal) {
		if (principal == null) {
			throw new BadCredentialsException("Authentication required");
		}
		String sseToken = jwtService.generateSseToken(
				principal.userId(), principal.tenantId(), principal.email(), principal.role()
		);
		return ResponseEntity.ok(new AuthDto.SseTokenResponse(sseToken, 300L));
	}
}