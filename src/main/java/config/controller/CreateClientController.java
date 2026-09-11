package config.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import config.DTO.Response;
import config.Entity.UserAccount;
import config.Entity.UserGroup;
import config.Service.CreateClientService;
import config.Service.UserGroupService;
import config.commonConfig.AppConstants;
import config.commonConfig.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
@Tag(name = "Client & User Groups", description = "Client creation, navigation and user-group CRUD")
public class CreateClientController {

	private static final Logger logger = LoggerFactory.getLogger(CreateClientController.class);

	private final CreateClientService clientService;
	private final UserGroupService userGroupService;

	@Operation(summary = "List user groups")
	@GetMapping(path = "/getUsergroupList")
	public Response getUsergroupList() {
		try {
			return clientService.getUserGroupList();
		} catch (Exception e) {
			logger.error("getUsergroupList failed", e);
			return ResponseBuilder.error(AppConstants.ERROR_SOMETHING_WENT_WRONG);
		}
	}

	@Operation(summary = "Get navigation menu by user group")
	@GetMapping(path = "/getNavigationMenu/{userGroupId}")
	public Response getNavigationMenu(@PathVariable Long userGroupId) {
		try {
			return clientService.getNavigationList(userGroupId);
		} catch (Exception e) {
			logger.error("getNavigationMenu failed", e);
			return ResponseBuilder.error(AppConstants.ERROR_SOMETHING_WENT_WRONG);
		}
	}

	@Operation(summary = "Create user/client with Aadhaar and PAN uploads")
	@PostMapping(value = "/createUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Response createUser(@RequestPart("adharFile") MultipartFile adharFile,
			@RequestPart("panFile") MultipartFile panFile, @RequestPart("data") UserAccount userAccount) {
		try {
			return clientService.createClient(adharFile, panFile, userAccount);
		} catch (Exception e) {
			logger.error("createUser failed", e);
			return ResponseBuilder.error(AppConstants.ERROR_SOMETHING_WENT_WRONG);
		}
	}

	@Operation(summary = "Get user group by id")
	@GetMapping("/userGroups/{userGroupId}")
	public Response getUserGroupById(@PathVariable Long userGroupId) {
		return userGroupService.getUserGroupById(userGroupId);
	}

	@Operation(summary = "Create user group")
	@PostMapping("/createUserGroup")
	public Response createUserGroup(@RequestBody UserGroup userGroup) {
		return userGroupService.createUserGroup(userGroup);
	}

	@Operation(summary = "Update user group")
	@PostMapping("/updateUserGroup")
	public Response updateUserGroup(@RequestBody UserGroup userGroup) {
		return userGroupService.updateUserGroup(userGroup);
	}

	@Operation(summary = "Delete user group")
	@PostMapping("/deleteUserGroup")
	public Response deleteUserGroup(@RequestBody UserGroup userGroup) {
		return userGroupService.deleteUserGroup(userGroup.getUserGroupId());
	}
}
