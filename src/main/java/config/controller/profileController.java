package config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.ProfileService;

@CrossOrigin(origins = "*")
@RestController()
@RequestMapping(path = "/api")
public class ProfileController {

	@Autowired
	ProfileService profileS;

	@RequestMapping(value = "/getUserProfile", method = RequestMethod.POST, produces = "application/json")
	public String getUserProfile(UserAccount userAccount) {
		try {
			return profileS.getUserProfile(userAccount);
		} catch (Exception e) {
			return "Something Went Wrong";
		}

	}

	@RequestMapping(value = "/checkCurrentPassword", method = RequestMethod.POST, produces = "application/Json")
	public Response checkCurrentPassword(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			return profileS.checkCurrentPassword(userAccount);
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("Current Password Is Wrong");
			return response;
		}
	}

	@RequestMapping(value = "/changePassword", method = RequestMethod.POST, produces = "application/Json")
	public Response changePassword(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			return profileS.changePassword(userAccount);
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("Invalid Data");
			return response;
		}
	}

	@RequestMapping(value = "/updateUserProfile", method = RequestMethod.POST, produces = "application/Json")
	public Response updateUserProfile(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			return profileS.updateUserProfile(userAccount);
		} catch (Exception e) {
			response.setMessage("Invalid Data");
			response.setStatus(false);
			return response;
		}
	}

}
