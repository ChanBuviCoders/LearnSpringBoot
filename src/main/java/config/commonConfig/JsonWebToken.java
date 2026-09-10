package config.commonConfig;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import config.Entity.UserAccount;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JsonWebToken {
	private static String secretKey = "this is secretkey that needs to be at least 256 bits long for HS512";
	private static SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

	public String generateToken(UserAccount userAccount) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userAccountId", userAccount.getUserAccountId());

		return Jwts.builder()
				.subject(String.valueOf(userAccount.getUserName()))
				.claims(claims)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + (120 * 60 * 1000)))
				.signWith(key, SignatureAlgorithm.HS512)
				.compact();
	}

	public void verifyToken(String token) {
		try {
			Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token);
		} catch (Exception e) {
			System.out.println("Exception in token: " + e);
			throw new AccessDeniedException("Token was expired or invalid");
		}
	}

}
