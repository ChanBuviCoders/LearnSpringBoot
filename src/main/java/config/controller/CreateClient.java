package config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.CreateClientService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/api")
public class CreateClient {

	@Autowired
	CreateClientService clientService;

	@GetMapping(path = "/getUsergroupList")
	public Response getUsergroupList() {
		Response response = new Response();
		try {
			return clientService.getUserGroupList();
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("somthing went wrong");
			return response;
		}
	}

	@GetMapping(path = "/getNavigationMenu/{userGroupId}")
	public Response getNavigetNavigationMenugationMenu(@PathVariable Long userGroupId) {
		Response response = new Response();
		try {
			return clientService.getNavigationList(userGroupId);
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("somthing went wrong");
			return response;
		}
	}

	@RequestMapping(value = "/createUser", method = RequestMethod.POST, produces = "application/json")
	public Response createUser(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			return clientService.createClient(userAccount);
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("somthing went wrong");
			return response;
		}
	}

}