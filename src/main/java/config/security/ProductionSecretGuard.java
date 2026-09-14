package config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
class ProductionSecretGuard {

	ProductionSecretGuard(@Value("${app.jwt.secret}") String jwtSecret) {
		if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.contains("this-is-secretkey")) {
			throw new IllegalStateException("JWT_SECRET must be set to a unique value in production");
		}
	}
}
