package config.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import config.DAO.UserAccountR;
import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User account CRUD APIs")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	private final UserAccountService userAccountService;
	private final UserAccountR userAccountR;

	@Value("${app.mail.default-to:}")
	private String defaultMailTo;

	@Value("${app.mail.default-subject:Notification}")
	private String defaultMailSubject;

	@Value("${app.mail.default-text:Hello from Learn Spring Boot}")
	private String defaultMailText;

	@Operation(summary = "List all users (passwords excluded)")
	@GetMapping(value = "/getAllUser", produces = "application/json")
	public List<UserAccount> getAllUser() {
		List<UserAccount> users = userAccountR.findAll();
		users.forEach(user -> user.setPassword(null));
		return users;
	}

	@Operation(summary = "Get user by id")
	@GetMapping(value = "/users/{userAccountId}", produces = "application/json")
	public Response getUserById(@PathVariable Long userAccountId) {
		return userAccountService.getUserById(userAccountId);
	}

	@Operation(summary = "Update user")
	@PostMapping(value = "/updateUser", produces = "application/json")
	public Response updateUser(@RequestBody UserAccount userAccount) {
		return userAccountService.updateUser(userAccount);
	}

	@Operation(summary = "Delete user")
	@PostMapping(value = "/deleteUser", produces = "application/json")
	public Response deleteUser(@RequestBody UserAccount userAccount) {
		return userAccountService.deleteUser(userAccount.getUserAccountId());
	}

	@Operation(summary = "Send email using configured mail sender")
	@PostMapping(value = "/sendMail", produces = "application/json")
	public Response sendMail(@RequestParam(required = false) String to,
			@RequestParam(required = false) String subject,
			@RequestParam(required = false) String text) {
		String mailTo = (to != null && !to.isBlank()) ? to : defaultMailTo;
		if (mailTo == null || mailTo.isBlank()) {
			Response response = new Response();
			response.setStatus(false);
			response.setMessage("Mail recipient is not configured");
			return response;
		}
		logger.info("sendMail requested to={}", mailTo);
		return userAccountService.sendMail(mailTo,
				subject != null ? subject : defaultMailSubject,
				text != null ? text : defaultMailText);
	}
}
