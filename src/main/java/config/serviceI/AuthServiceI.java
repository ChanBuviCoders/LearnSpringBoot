package config.serviceI;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import config.DAO.UserAccountR;
import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.AuthService;
import config.Service.AzureBlobService;
import config.commonConfig.JsonWebToken;
import config.security.PasswordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceI implements AuthService {

	private static final Logger logger = LoggerFactory.getLogger(AuthServiceI.class);

	private final UserAccountR userAccountR;
	private final JsonWebToken jwtService;
	private final AzureBlobService azureBlobService;
	private final PasswordService passwordService;

	@Override
	public Response authSession(UserAccount userAccount) {
		Response response = new Response();
		try {
			UserAccount ua = userAccountR.findByUserName(userAccount.getUserName());
			if (ua == null) {
				response.setStatus(false);
				response.setMessage("Invalid username or password");
				return response;
			}

			if (ua.getLoginAttempt() != null && ua.getLoginAttempt() >= 3) {
				response.setStatus(false);
				response.setMessage("Your account has been locked");
				return response;
			}

			if (passwordService.matches(userAccount.getPassword(), ua.getPassword())) {
				ua.setActive(true);
				ua.setLoginAttempt(0);
				if (passwordService.needsUpgrade(ua.getPassword())) {
					ua.setPassword(passwordService.encode(userAccount.getPassword()));
					logger.info("Upgraded password hash for user {}", ua.getUserName());
				}
				userAccountR.save(ua);
				response.setToken(jwtService.generateToken(ua));
				response.setStatus(true);
				response.setMessage("User Authentication Success");
				logger.info("User authenticated successfully: {}", ua.getUserName());
			} else {
				int attempts = ua.getLoginAttempt() == null ? 0 : ua.getLoginAttempt();
				ua.setActive(false);
				ua.setLoginAttempt(attempts + 1);
				userAccountR.save(ua);
				int remaining = 3 - ua.getLoginAttempt();
				response.setStatus(false);
				if (ua.getLoginAttempt() >= 3) {
					response.setMessage("Your account has been locked");
				} else {
					response.setMessage("Incorrect password, you have only " + remaining + " more attempt"
							+ (remaining > 1 ? "s" : ""));
				}
				logger.warn("Failed login attempt for user {}", userAccount.getUserName());
			}
			return response;
		} catch (Exception e) {
			logger.error("Authentication failure", e);
			response.setStatus(false);
			response.setMessage("User Authentication Failure");
			return response;
		}
	}

	@Override
	public Response getSession(String jwtToken) {
		Response response = new Response();
		try {
			String token = jwtService.stripBearerPrefix(jwtToken);
			jwtService.verifyToken(token);
			Long userAccountId = jwtService.getUserAccountIdFromToken(token);
			return getTokenUser(userAccountId, token);
		} catch (Exception e) {
			logger.error("Failed to resolve session from token", e);
			response.setStatus(false);
			response.setMessage("Failure");
			response.setError("Something Went Wrong");
			return response;
		}
	}

	public Response getTokenUser(Long userAccountId, String jwtToken) {
		Response response = new Response();
		try {
			if (userAccountId == null) {
				response.setStatus(false);
				response.setMessage(jwtToken);
				return response;
			}

			UserAccount cudb = userAccountR.findByUserAccountId(userAccountId);
			if (cudb == null) {
				response.setStatus(false);
				response.setMessage("User not found");
				return response;
			}

			if (cudb.getPanImagePath() != null) {
				cudb.setPanImagePath(azureBlobService.generateReadToken(cudb.getPanImagePath()));
			}
			if (cudb.getAdharImagePath() != null) {
				cudb.setAdharImagePath(azureBlobService.generateReadToken(cudb.getAdharImagePath()));
			}
			cudb.setPassword(null);
			response.setStatus(true);
			response.setMessage("success");
			response.setData(cudb);
			response.setToken(jwtToken);
			return response;
		} catch (Exception e) {
			logger.error("Failed to load token user {}", userAccountId, e);
			response.setStatus(false);
			response.setMessage("Failure");
			response.setError("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response logout(UserAccount userAccount) {
		Response response = new Response();
		try {
			UserAccount userAccountFr = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			String fDate = formatter.format(date);
			java.util.Date dateType = formatter.parse(fDate);
			java.sql.Date sqlStartDate = new java.sql.Date(dateType.getTime());
			userAccountFr.setLastLoginDate(sqlStartDate);
			userAccountR.save(userAccountFr);
			response.setStatus(true);
			response.setMessage("Logout successful");
			logger.info("User logged out: {}", userAccountFr.getUserName());
		} catch (Exception e) {
			logger.error("Logout failed", e);
			response.setStatus(false);
			response.setMessage("Logout failed");
		}
		return response;
	}
}
