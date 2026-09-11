package config.serviceI;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import config.DAO.UserAccountR;
import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.UserAccountService;
import config.security.PasswordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAccountServiceI implements UserAccountService {

	private static final Logger logger = LoggerFactory.getLogger(UserAccountServiceI.class);

	private final UserAccountR userAccountR;
	private final PasswordService passwordService;
	private final JavaMailSender mailSender;

	@Override
	public Response getAllUsers() {
		Response response = new Response();
		try {
			List<UserAccount> users = userAccountR.findAll();
			users.forEach(user -> user.setPassword(null));
			response.setData(users);
			response.setStatus(true);
			response.setMessage("Success");
			return response;
		} catch (Exception e) {
			logger.error("getAllUsers failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response getUserById(Long userAccountId) {
		Response response = new Response();
		try {
			UserAccount user = userAccountR.findByUserAccountId(userAccountId);
			if (user == null) {
				response.setStatus(false);
				response.setMessage("User not found");
				return response;
			}
			user.setPassword(null);
			response.setData(user);
			response.setStatus(true);
			response.setMessage("Success");
			return response;
		} catch (Exception e) {
			logger.error("getUserById failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response updateUser(UserAccount userAccount) {
		Response response = new Response();
		try {
			UserAccount existing = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
			if (existing == null) {
				response.setStatus(false);
				response.setMessage("User not found");
				return response;
			}
			if (userAccount.getFirstName() != null) {
				existing.setFirstName(userAccount.getFirstName());
			}
			if (userAccount.getLastName() != null) {
				existing.setLastName(userAccount.getLastName());
			}
			if (userAccount.getEmail() != null) {
				existing.setEmail(userAccount.getEmail());
			}
			if (userAccount.getGender() != null) {
				existing.setGender(userAccount.getGender());
			}
			if (userAccount.getMobileNumber() != null) {
				existing.setMobileNumber(userAccount.getMobileNumber());
			}
			if (userAccount.getUserGroupId() != null) {
				existing.setUserGroupId(userAccount.getUserGroupId());
			}
			if (userAccount.getPassword() != null && !userAccount.getPassword().isBlank()) {
				existing.setPassword(passwordService.encode(userAccount.getPassword()));
			}
			userAccountR.save(existing);
			existing.setPassword(null);
			response.setData(existing);
			response.setStatus(true);
			response.setMessage("User updated successfully");
			logger.info("User updated: {}", existing.getUserAccountId());
			return response;
		} catch (Exception e) {
			logger.error("updateUser failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response deleteUser(Long userAccountId) {
		Response response = new Response();
		try {
			if (!userAccountR.existsById(userAccountId)) {
				response.setStatus(false);
				response.setMessage("User not found");
				return response;
			}
			userAccountR.deleteById(userAccountId);
			response.setStatus(true);
			response.setMessage("User deleted successfully");
			logger.info("User deleted: {}", userAccountId);
			return response;
		} catch (Exception e) {
			logger.error("deleteUser failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response sendMail(String to, String subject, String text) {
		Response response = new Response();
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject(subject != null ? subject : "Notification");
			message.setText(text != null ? text : "");
			mailSender.send(message);
			response.setStatus(true);
			response.setMessage("Mail sent successfully");
			logger.info("Mail sent to {}", to);
			return response;
		} catch (Exception e) {
			logger.error("sendMail failed", e);
			response.setStatus(false);
			response.setMessage("Failed to send mail");
			return response;
		}
	}
}
