package com.btg.funds.entrypoints.rest;

import com.btg.funds.domain.exception.ClientNotFoundException;
import com.btg.funds.domain.exception.FundNotFoundException;
import com.btg.funds.domain.exception.InsufficientBalanceException;
import com.btg.funds.domain.exception.SubscriptionNotFoundException;
import com.btg.funds.entrypoints.rest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class RestExceptionHandler {
	@ExceptionHandler({
			ClientNotFoundException.class,
			FundNotFoundException.class,
			SubscriptionNotFoundException.class
	})
	public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, ex, request);
	}

	@ExceptionHandler({
			InsufficientBalanceException.class,
			IllegalArgumentException.class
	})
	public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, ex, request);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, ex, request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		var first = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
		String message = first == null ? "Validation error" : (first.getField() + ": " + first.getDefaultMessage());
		return error(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
	}

	private ResponseEntity<ErrorResponse> error(HttpStatus status, Exception ex, HttpServletRequest request) {
		return error(status, ex.getMessage(), request);
	}

	private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
		return ResponseEntity.status(status).body(new ErrorResponse(
				Instant.now(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		));
	}
}
