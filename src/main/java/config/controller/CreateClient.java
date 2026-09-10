package config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.CreateClientService;
import config.commonConfig.AppConstants;
import config.commonConfig.ResponseBuilder;

@RestController
@RequestMapping(path = "/api")
public class CreateClient {

	@Autowired
	CreateClientService clientService;

	@GetMapping(path = "/getUsergroupList")
	public Response getUsergroupList() {
		try {
			return clientService.getUserGroupList();
		} catch (Exception e) {
			return ResponseBuilder.error(AppConstants.ERROR_SOMETHING_WENT_WRONG);
		}
	}

	@GetMapping(path = "/getNavigationMenu/{userGroupId}")
	public Response getNavigationMenu(@PathVariable Long userGroupId) {
		try {
			return clientService.getNavigationList(userGroupId);
		} catch (Exception e) {
			return ResponseBuilder.error(AppConstants.ERROR_SOMETHING_WENT_WRONG);
		}
	}

	@PostMapping(value = "/createUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Response createUser(@RequestPart("adharFile") MultipartFile adharFile,
			@RequestPart("panFile") MultipartFile panFile, @RequestPart("data") UserAccount userAccount) {
		try {
			return clientService.createClient(adharFile, panFile, userAccount);
		} catch (Exception e) {
			return ResponseBuilder.error(AppConstants.ERROR_SOMETHING_WENT_WRONG);
		}
	}

}