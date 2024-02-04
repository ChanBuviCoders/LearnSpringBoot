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
	
	@RequestMapping(value = "/createUser", method = RequestMethod.POST, produces = "application/json")
	public response createUser(@RequestBody userAccount userAccount) {
		response response = new response();
		try {
			return ClientService.createClient(userAccount);
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("somthing went wrong");
			return response;
		}
	}

}