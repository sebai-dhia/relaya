package com.relaya.demo.config.web;

import com.relaya.demo.analysis.adapter.out.ai.BudgetExceededException;
import com.relaya.demo.review.domain.DraftMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage())
				.findFirst()
				.orElse("Validation failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiErrorResponse.of("VALIDATION_FAILED", message));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
		String code = ex.getMessage() != null && ex.getMessage().contains("exceeds") ? "INPUT_TOO_LONG" : "BAD_REQUEST";
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiErrorResponse.of(code, ex.getMessage()));
	}

	@ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
	public ResponseEntity<ApiErrorResponse> handleBadCredentials(Exception ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiErrorResponse.of("INVALID_CREDENTIALS", "Invalid email or password"));
	}

	@ExceptionHandler(BudgetExceededException.class)
	public ResponseEntity<ApiErrorResponse> handleBudgetExceeded(BudgetExceededException ex) {
		String code = ex.getMessage() != null && ex.getMessage().contains("cap") ? "INPUT_TOO_LONG" : "BUDGET_EXHAUSTED";
		HttpStatus status = "INPUT_TOO_LONG".equals(code) ? HttpStatus.BAD_REQUEST : HttpStatus.PAYMENT_REQUIRED;
		return ResponseEntity.status(status)
				.body(ApiErrorResponse.of(code, ex.getMessage()));
	}

	@ExceptionHandler(DraftMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleDraftMismatch(DraftMismatchException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ApiErrorResponse.of("APPROVAL_INVALIDATED", ex.getMessage()));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ApiErrorResponse.of("FORBIDDEN", "Access is denied"));
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(NoSuchElementException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiErrorResponse.of("RESOURCE_NOT_FOUND", ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(IllegalStateException ex) {
		String code = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("pending") ? "ANALYSIS_IN_PROGRESS" : "CONFLICT";
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ApiErrorResponse.of(code, ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiErrorResponse.of("INTERNAL_ERROR", ex.getMessage()));
	}
}