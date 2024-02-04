package config.serviceI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.gson.JsonObject;

import config.DAO.userAccountR;
import config.DTO.response;
import config.Entity.userAccount;
import config.Service.profileService;

@Service
public class profileServiceI implements profileService {

	@Autowired
	userAccountR userAccountR;

	@Override
	public String getUserProfile(userAccount userAccount) {
		JsonObject jsonObj = new JsonObject();
		try {
			jsonObj.addProperty("item", userAccountR.findById(userAccount.getUserAccountId()).toString());
			return jsonObj.toString();

		} catch (Exception e) {

			return "Something Went Wrong";
		}

	}

	@Override
	public response checkCurrentPassword(@RequestBody userAccount userAccount) {
		response response = new response();
		try {
			userAccount userAccountFr = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
			if (userAccountFr.getPassword().equals(userAccount.getPassword())) {
				response.setStatus(true);
				response.setMessage("Success");
			} else {

				response.setStatus(false);
				response.setMessage("Current Password Is Wrong");
			}
			return response;
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("Current Password Is Wrong");
			return response;
		}
	}

	@Override
	public response changePassword(@RequestBody userAccount userAccount) {
		response response = new response();
		try {

			if (userAccount.getUserAccountId() != null && !userAccount.getPassword().isEmpty()) {
				userAccount userAccountFr = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
				userAccountFr.setPassword(userAccount.getPassword());
				userAccountR.save(userAccountFr);
				response.setStatus(true);
				response.setMessage("Password Changed Successfully");
			} else {

				response.setStatus(false);
				response.setMessage("Something Went Wrong");
			}
			return response;
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("Invalid Data");
			return response;
		}
	}

	@Override
	public response updateUserProfile(@RequestBody userAccount userAccount) {
		response response = new response();
		try {
			if (userAccount.getUserAccountId() != null) {
				/************** Fr -->from repository ******************/
				userAccount userAccountFr = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
				userAccountFr.setFirstName(userAccount.getFirstName());
				userAccountFr.setLastName(userAccount.getLastName());
				userAccountFr.setDob(userAccount.getDob());
				userAccountFr.setEmail(userAccount.getEmail());
				userAccountFr.setGender(userAccount.getGender());
				userAccountR.save(userAccountFr);
				response.setMessage("Updated Successfully");
				response.setStatus(true);
				return response;
			} else {
				response.setMessage("Something Went Wrong");
				response.setStatus(false);
				return response;
			}
		} catch (Exception e) {
			response.setMessage("Invalid Data");
			response.setStatus(false);
			return response;
		}
	}

}
