package config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import config.DTO.response;
import config.Entity.userAccount;
import config.Service.profileService;

@CrossOrigin(origins = "*")
@RestController()
@RequestMapping(path = "/api")
public class profileController {

	@Autowired
	profileService profileS;

	@RequestMapping(value = "/getUserProfile", method = RequestMethod.POST, produces = "application/json")
	public String getUserProfile(userAccount userAccount) {
		try {
			return profileS.getUserProfile(userAccount);
		} catch (Exception e) {
			return "Something Went Wrong";
		}

	}

	@RequestMapping(value = "/checkCurrentPassword", method = RequestMethod.POST, produces = "application/Json")
	public response checkCurrentPassword(@RequestBody userAccount userAccount) {
		response response = new response();
		try {
			return profileS.checkCurrentPassword(userAccount);
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("Current Password Is Wrong");
			return response;
		}
	}

	@RequestMapping(value = "/changePassword", method = RequestMethod.POST, produces = "application/Json")
	public response changePassword(@RequestBody userAccount userAccount) {
		response response = new response();
		try {
			return profileS.changePassword(userAccount);
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("Invalid Data");
			return response;
		}
	}

	@RequestMapping(value = "/updateUserProfile", method = RequestMethod.POST, produces = "application/Json")
	public response updateUserProfile(@RequestBody userAccount userAccount) {
		response response = new response();
		try {
			return profileS.updateUserProfile(userAccount);
		} catch (Exception e) {
			response.setMessage("Invalid Data");
			response.setStatus(false);
			return response;
		}
	}

}
