package config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import config.DTO.response;
import config.Entity.userAccount;
import config.Service.createClientService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/api")
public class createClient {

	@Autowired
	createClientService ClientService;

	@Autowired
	response response;

	@SuppressWarnings("unused")
	@RequestMapping(value = "/createUser", method = RequestMethod.POST, produces = "application/json")
	public response createUser(@RequestBody userAccount userAccount) {
		try {
			return ClientService.createClient(userAccount);
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("somthing went wrong");
			return response;
		}
	}

	/********************************************
	 * this method currently not in use
	 ********************************************/
//	@RequestMapping(value = "/saveLoginCred", method = RequestMethod.POST, produces = "application/json")
//	public response saveLoginCred(@RequestBody LoginCred loginCred) {
//
//		if (loginCred != null) {
//			LoginCred lc = ClientService.getUserByUserId(loginCred.getUserId());
//			if (lc != null) {
//				ClientService.save(loginCred);
//				response.setMessage("Created Successfully");
//				response.setStatus(true);
//				return response;
//			} else {
//				response.setMessage("Userid already taken");
//				response.setStatus(false);
//				return response;
//			}
//
//		} else {
//			response.setMessage("Invalid Data");
//			response.setStatus(false);
//			return response;
//		}
//
//	}
}