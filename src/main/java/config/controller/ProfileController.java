package config.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile and password APIs")
public class ProfileController {

	private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

	private final ProfileService profileService;

	@Operation(summary = "Get user profile by userAccountId")
	@PostMapping(value = "/getUserProfile", produces = "application/json")
	public Response getUserProfile(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			return profileService.getUserProfile(userAccount);
		} catch (Exception e) {
			logger.error("getUserProfile failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Operation(summary = "Verify current password")
	@PostMapping(value = "/checkCurrentPassword", produces = "application/json")
	public Response checkCurrentPassword(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			return profileService.checkCurrentPassword(userAccount);
		} catch (Exception e) {
			logger.error("checkCurrentPassword failed", e);
			response.setStatus(false);
			response.setMessage("Current Password Is Wrong");
			return response;
		}
	}

	@Operation(summary = "Change password")
	@PostMapping(value = "/changePassword", produces = "application/json")
	public Response changePassword(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			return profileService.changePassword(userAccount);
		} catch (Exception e) {
			logger.error("changePassword failed", e);
			response.setStatus(false);
			response.setMessage("Invalid Data");
			return response;
		}
	}

	@Operation(summary = "Update profile fields")
	@PostMapping(value = "/updateUserProfile", produces = "application/json")
	public Response updateUserProfile(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			return profileService.updateUserProfile(userAccount);
		} catch (Exception e) {
			logger.error("updateUserProfile failed", e);
			response.setMessage("Invalid Data");
			response.setStatus(false);
			return response;
		}
	}
}
