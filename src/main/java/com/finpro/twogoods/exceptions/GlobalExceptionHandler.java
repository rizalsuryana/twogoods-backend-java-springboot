package com.finpro.twogoods.exceptions;

import com.cloudinary.api.exceptions.ApiException;
import com.finpro.twogoods.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	//   1. API Exception (custom)
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<?> handleApiException(ApiException ex) {
		log.error("API error: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				ex.getMessage(),
				List.of(ex.getMessage())
		);
	}

	//   2. Resource Not Found
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
		log.error("Not found: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.NOT_FOUND,
				ex.getMessage(),
				List.of(ex.getMessage())
		);
	}

	//   3. Duplicate Resource
	@ExceptionHandler(ResourceDuplicateException.class)
	public ResponseEntity<?> handleDuplicate(ResourceDuplicateException ex) {
		log.error("Duplicate: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				ex.getMessage(),
				List.of(ex.getMessage())
		);
	}

	//   4. Invalid File
	@ExceptionHandler(InvalidFileException.class)
	public ResponseEntity<?> handleInvalidFile(InvalidFileException ex) {
		log.error("Invalid file: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				ex.getMessage(),
				List.of(ex.getMessage())
		);
	}

	//   5. Validation Errors (@Valid)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
		log.error("Validation error: {}", ex.getMessage());

		Map<String, String> fieldErrors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(err ->
				fieldErrors.put(err.getField(), err.getDefaultMessage())
		);

		return ResponseUtil.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				"Validation failed",
				fieldErrors.values().stream().toList()
		);
	}

	//   6. Illegal Argument
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
		log.error("Illegal argument: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				ex.getMessage(),
				List.of(ex.getMessage())
		);
	}

	//   7. Rate Limit
	@ExceptionHandler(RateLimitException.class)
	public ResponseEntity<?> handleRateLimit(RateLimitException ex) {
		log.error("Rate limit: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.TOO_MANY_REQUESTS,
				ex.getMessage(),
				List.of(ex.getMessage())
		);
	}

	//   8. Multipart Error
	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<?> handleMultipart(MultipartException ex) {
		log.error("Multipart error: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				"Invalid or too large file upload",
				List.of(ex.getMessage())
		);
	}

	//   9. JSON Parse Error
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> handleJsonParse(HttpMessageNotReadableException ex) {
		log.error("JSON parse error: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				"Invalid JSON format",
				List.of(ex.getMessage())
		);
	}

	//   10. Missing Request Parameter
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
		log.error("Missing parameter: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				ex.getMessage(),
				List.of(ex.getMessage())
		);
	}

	//   11. Constraint Violation
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
		log.error("Constraint violation: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				ex.getMessage(),
				List.of(ex.getMessage())
		);
	}

	//   12. Method Not Allowed
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<?> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
		log.error("Method not allowed: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.METHOD_NOT_ALLOWED,
				"Method not allowed",
				List.of(ex.getMessage())
		);
	}

	//   13. Endpoint Not Found
	@ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
	public ResponseEntity<?> handleNoHandler(org.springframework.web.servlet.NoHandlerFoundException ex) {
		log.error("Endpoint not found: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.NOT_FOUND,
				"Endpoint not found",
				List.of(ex.getMessage())
		);
	}

	//   14. Database Constraint Error
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
		log.error("DB constraint error: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				"Database constraint violation",
				List.of(ex.getMessage())
		);
	}

	//   15. Authentication Errors
	@ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
	public ResponseEntity<?> handleBadCredentials(Exception ex) {
		log.error("Bad credentials: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.UNAUTHORIZED,
				"Unauthorized",
				List.of(ex.getMessage())
		);
	}

	//   16. Account Locked / Disabled
	@ExceptionHandler({LockedException.class, DisabledException.class})
	public ResponseEntity<?> handleAccountLocked(Exception ex) {
		log.error("Account locked/disabled: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.FORBIDDEN,
				"Forbidden Resource",
				List.of(ex.getMessage())
		);
	}

	//   17. JWT Errors
	@ExceptionHandler(JwtAuthenticationException.class)
	public ResponseEntity<?> handleJwt(JwtAuthenticationException ex) {
		log.error("JWT error: {}", ex.getMessage());
		return ResponseUtil.buildErrorResponse(
				HttpStatus.UNAUTHORIZED,
				"Invalid or expired token",
				List.of(ex.getMessage())
		);
	}

	// 18. Fallback (General Exception)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleGeneral(Exception ex) {

		if (ex instanceof AccessDeniedException) throw (AccessDeniedException) ex;
		if (ex instanceof AuthenticationException) throw (AuthenticationException) ex;

		log.error("Internal error: {}", ex.getMessage(), ex);

		return ResponseUtil.buildErrorResponse(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Internal Server Error",
				List.of(ex.getMessage())
		);
	}
}
