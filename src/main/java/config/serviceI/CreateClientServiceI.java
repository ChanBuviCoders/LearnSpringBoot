package config.serviceI;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import config.DAO.NavigationR;
import config.DAO.UserAccountR;
import config.DAO.UserGroupR;
import config.DTO.NavigationMenuDTO;
import config.DTO.Response;
import config.Entity.UserAccount;
import config.Entity.UserGroup;
import config.Service.AzureBlobService;
import config.Service.CreateClientService;
import config.security.PasswordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateClientServiceI implements CreateClientService {

	private static final Logger logger = LoggerFactory.getLogger(CreateClientServiceI.class);

	private final UserAccountR userAccountR;
	private final UserGroupR userGroupR;
	private final NavigationR navigationR;
	private final AzureBlobService storageService;
	private final PasswordService passwordService;

	@Override
	public Response createClient(MultipartFile adharFile, MultipartFile panFile, UserAccount userAccount) {
		Response response = new Response();
		try {
			UserAccount existing = userAccountR.getUserAccountByUserName(userAccount.getUserName());
			if (existing != null) {
				response.setStatus(false);
				response.setMessage("UserId Already Taken");
				return response;
			}

			userAccount.setAdharImagePath(storageService.uploadFile(adharFile, "ADHAR"));
			userAccount.setPanImagePath(storageService.uploadFile(panFile, "PAN"));
			userAccount.setLoginAttempt(0);
			if (userAccount.getPassword() != null && !userAccount.getPassword().isBlank()) {
				userAccount.setPassword(passwordService.encode(userAccount.getPassword()));
			}
			userAccountR.save(userAccount);
			response.setStatus(true);
			response.setMessage("Created Successfully");
			logger.info("Client user created: {}", userAccount.getUserName());
			return response;
		} catch (Exception e) {
			logger.error("createClient failed", e);
			response.setStatus(false);
			response.setMessage("Something went wrong");
			return response;
		}
	}

	@Override
	public Response getUserGroupList() {
		Response response = new Response();
		try {
			List<UserGroup> userGroups = userGroupR.findAll();
			response.setData(userGroups);
			response.setStatus(true);
			response.setMessage("Success");
			return response;
		} catch (Exception e) {
			logger.error("getUserGroupList failed", e);
			response.setStatus(false);
			response.setMessage("Something went wrong");
			return response;
		}
	}

	@Override
	public Response getNavigationList(Long userGroupId) {
		Response response = new Response();
		try {
			List<NavigationMenuDTO> navigationMenuList = navigationR.getNavigationMenuByUserGroupId(userGroupId)
					.stream()
					.map(menu -> new NavigationMenuDTO((Long) menu[0], (String) menu[1], (Boolean) menu[2],
							(Boolean) menu[3], (Boolean) menu[4]))
					.collect(Collectors.toList());
			response.setData(navigationMenuList);
			response.setStatus(true);
			response.setMessage("Success");
			return response;
		} catch (Exception e) {
			logger.error("getNavigationList failed for userGroupId={}", userGroupId, e);
			response.setStatus(false);
			response.setMessage("Something went wrong");
			return response;
		}
	}
}
