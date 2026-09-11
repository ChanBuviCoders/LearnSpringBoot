package config.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Supports legacy plaintext passwords and migrates them to BCrypt on successful match.
 */
@Service
@RequiredArgsConstructor
public class PasswordService {

	private final PasswordEncoder passwordEncoder;

	public String encode(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	public boolean matches(String rawPassword, String storedPassword) {
		if (rawPassword == null || storedPassword == null) {
			return false;
		}
		if (isBcryptHash(storedPassword)) {
			return passwordEncoder.matches(rawPassword, storedPassword);
		}
		return storedPassword.equals(rawPassword);
	}

	public boolean needsUpgrade(String storedPassword) {
		return storedPassword != null && !isBcryptHash(storedPassword);
	}

	private boolean isBcryptHash(String value) {
		return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
	}
}
