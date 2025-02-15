package config.commonConfig;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice // for handle exception globally (class annotation)
public class GlobalExceptionConfig {

	@ExceptionHandler // for handle exception globally (method annotation)
	private ResponseEntity<?> arithmeticException(ArithmeticException AE) {
		System.out.println(AE);
		return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(null);
	}

	@ExceptionHandler
	private ResponseEntity<String> accessDeniedException(AccessDeniedException ADE) {

		System.out.println(ADE);
		return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body("Full Authentication Error");
	}
}
