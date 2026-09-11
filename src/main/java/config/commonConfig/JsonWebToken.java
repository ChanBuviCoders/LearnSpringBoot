package config.commonConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import config.Entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JsonWebToken {

	private static final Logger logger = LoggerFactory.getLogger(JsonWebToken.class);

	private final SecretKey key;
	private final long expirationMs;

	public JsonWebToken(
			@Value("${app.jwt.secret:this-is-secretkey-that-needs-to-be-at-least-256-bits-long-for-HS512}") String secret,
			@Value("${app.jwt.expiration:7200000}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.expirationMs = expirationMs;
	}

	public String generateToken(UserAccount userAccount) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userAccountId", userAccount.getUserAccountId());
		if (userAccount.getUserGroupId() != null) {
			claims.put("userGroupId", userAccount.getUserGroupId());
		}

		return Jwts.builder()
				.subject(String.valueOf(userAccount.getUserName()))
				.claims(claims)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expirationMs))
				.signWith(key)
				.compact();
	}

	public void verifyToken(String token) {
		try {
			parseClaims(token);
		} catch (Exception e) {
			logger.warn("Token verification failed: {}", e.getMessage());
			throw new AccessDeniedException("Token was expired or invalid");
		}
	}

	public String getUsernameFromToken(String token) {
		return parseClaims(token).getSubject();
	}

	public Long getUserAccountIdFromToken(String token) {
		Object value = parseClaims(token).get("userAccountId");
		if (value instanceof Number number) {
			return number.longValue();
		}
		return value != null ? Long.valueOf(value.toString()) : null;
	}

	public String stripBearerPrefix(String token) {
		if (token == null) {
			return null;
		}
		String trimmed = token.trim();
		if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return trimmed.substring(7).trim();
		}
		return trimmed;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(stripBearerPrefix(token))
				.getPayload();
	}
}
