package config.commonConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import config.DTO.Response;

/**
 * Global exception handler for centralized error handling and logging
 */
@ControllerAdvice
public class GlobalExceptionConfig {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionConfig.class);

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Response> responseStatusException(ResponseStatusException ex) {
		return ResponseEntity.status(ex.getStatusCode())
				.body(ResponseBuilder.error(ex.getReason()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Response> validationException(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.orElse("Request validation failed");
		return ResponseEntity.badRequest().body(ResponseBuilder.error(message));
	}

	@ExceptionHandler(ArithmeticException.class)
	public ResponseEntity<Response> arithmeticException(ArithmeticException ex) {
		logger.error("Arithmetic exception occurred: ", ex);
		Response response = new Response();
		response.setStatus(false);
		response.setMessage("Invalid mathematical operation");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Response> accessDeniedException(AccessDeniedException ex) {
		logger.error("Access denied exception occurred: ", ex);
		Response response = new Response();
		response.setStatus(false);
		response.setMessage("Full Authentication Required");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Response> handleGenericException(Exception ex) {
		logger.error("Unexpected exception occurred: ", ex);
		Response response = new Response();
		response.setStatus(false);
		response.setMessage("An unexpected error occurred. Please try again later.");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
