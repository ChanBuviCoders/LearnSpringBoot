package config.commonConfig;

import java.sql.Date;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import config.Entity.userAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class jsonWebToken {
	private static String secretKey = "this is secretkey";

	public String generateToken(userAccount userAccount) {

		Claims claims = Jwts.claims().setIssuer(String.valueOf(userAccount.getUserName()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + (120 * 60 * 1000)));
		claims.put("userAccountId", userAccount.getUserAccountId());

		return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secretKey).compact();
	}

	public void verifyToken(String token) {

		try {
			Jws<Claims> pc=Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
			Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception in token" + e);
			throw new AccessDeniedException("i think token was expired");
		}
	}

}
