package config.serviceI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import config.DAO.UserAccountR;
import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.ProfileService;
import config.security.PasswordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceI implements ProfileService {

	private static final Logger logger = LoggerFactory.getLogger(ProfileServiceI.class);

	private final UserAccountR userAccountR;
	private final PasswordService passwordService;

	@Override
	public Response getUserProfile(UserAccount userAccount) {
		Response response = new Response();
		try {
			UserAccount profile = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
			if (profile == null) {
				response.setStatus(false);
				response.setMessage("User not found");
				return response;
			}
			profile.setPassword(null);
			response.setStatus(true);
			response.setMessage("Success");
			response.setData(profile);
			return response;
		} catch (Exception e) {
			logger.error("getUserProfile failed for id {}", userAccount.getUserAccountId(), e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response checkCurrentPassword(UserAccount userAccount) {
		Response response = new Response();
		try {
			UserAccount userAccountFr = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
			if (passwordService.matches(userAccount.getPassword(), userAccountFr.getPassword())) {
				response.setStatus(true);
				response.setMessage("Success");
			} else {
				response.setStatus(false);
				response.setMessage("Current Password Is Wrong");
			}
			return response;
		} catch (Exception e) {
			logger.error("checkCurrentPassword failed", e);
			response.setStatus(false);
			response.setMessage("Current Password Is Wrong");
			return response;
		}
	}

	@Override
	public Response changePassword(UserAccount userAccount) {
		Response response = new Response();
		try {
			if (userAccount.getUserAccountId() != null && userAccount.getPassword() != null
					&& !userAccount.getPassword().isEmpty()) {
				UserAccount userAccountFr = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
				userAccountFr.setPassword(passwordService.encode(userAccount.getPassword()));
				userAccountFr.setLoginAttempt(0);
				userAccountR.save(userAccountFr);
				response.setStatus(true);
				response.setMessage("Password Changed Successfully");
				logger.info("Password changed for userAccountId={}", userAccount.getUserAccountId());
			} else {
				response.setStatus(false);
				response.setMessage("Something Went Wrong");
			}
			return response;
		} catch (Exception e) {
			logger.error("changePassword failed", e);
			response.setStatus(false);
			response.setMessage("Invalid Data");
			return response;
		}
	}

	@Override
	public Response updateUserProfile(UserAccount userAccount) {
		Response response = new Response();
		try {
			if (userAccount.getUserAccountId() != null) {
				UserAccount userAccountFr = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
				userAccountFr.setFirstName(userAccount.getFirstName());
				userAccountFr.setLastName(userAccount.getLastName());
				userAccountFr.setDob(userAccount.getDob());
				userAccountFr.setEmail(userAccount.getEmail());
				userAccountFr.setGender(userAccount.getGender());
				userAccountR.save(userAccountFr);
				response.setMessage("Updated Successfully");
				response.setStatus(true);
				return response;
			}
			response.setMessage("Something Went Wrong");
			response.setStatus(false);
			return response;
		} catch (Exception e) {
			logger.error("updateUserProfile failed", e);
			response.setMessage("Invalid Data");
			response.setStatus(false);
			return response;
		}
	}
}
