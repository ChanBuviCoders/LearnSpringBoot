package config.serviceI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import config.DAO.NavigationR;
import config.DAO.UserAccountR;
import config.DAO.UserGroupR;
import config.DTO.NavigationMenuDTO;
import config.DTO.Response;
import config.Entity.UserAccount;
import config.Entity.UserGroup;
import config.Service.CreateClientService;

@Service
public class CreateClientServiceI implements CreateClientService {

	@Autowired
	UserAccountR userAccountR;
	@Autowired
	UserGroupR userGroupR;
	@Autowired
	NavigationR navigationR;

	@SuppressWarnings("unused")
	@Override
	public Response createClient(UserAccount userAccount) {
		Response response = new Response();
		try {

			UserAccount uA = userAccountR.getUserAccountByUserName(userAccount.getUserName());
			if (uA == null) {
				if (userAccount != null) {
					userAccount.setAdharImagePath(
							base64ToFile(userAccount.getAdharImagePath(), userAccount.getMobileNumber(), "adharImage"));
					userAccount.setPanImagePath(
							base64ToFile(userAccount.getPanImagePath(), userAccount.getMobileNumber(), "panImage"));
					userAccount.setLoginAttempt(0);
					userAccountR.save(userAccount);
					response.setStatus(true);
					response.setMessage("Created Successfully");
					return response;

				} else {
					response.setStatus(false);
					response.setMessage("Something Went Wrong");
					return response;
				}
			} else {
				response.setStatus(false);
				response.setMessage("UserId Already Taken");
				return response;
			}

		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("somthing went wrong");
			return response;
		}
	}

	String clientDocumentsPath = "B:\\projects\\SpringBoot\\LearnSpringBoot-main\\documents\\clientDocuments";

	public String base64ToFile(String base64String, long mobileNumber, String documentName)
			throws FileNotFoundException, IOException {
		String filePath;
		try {
			String mobilenumber = String.valueOf(mobileNumber);
			byte[] byteData = Base64.getDecoder().decode(base64String);
			File fileDirectory = new File(clientDocumentsPath.concat("\\" + mobilenumber));

			if (!fileDirectory.exists() && !fileDirectory.isDirectory())
				fileDirectory.mkdir();

			filePath = clientDocumentsPath.concat("\\" + mobilenumber + "\\" + documentName + ".png");

			if (!new File(clientDocumentsPath.concat("\\" + mobilenumber + "\\" + documentName + ".png")).exists()) {
				try (FileOutputStream stream = new FileOutputStream(filePath)) {
					stream.write(byteData);

				} catch (Exception e) {
					System.out.println("exception----methodname---base64ToFile-la" + e);
				}
			} else {

			}
		} catch (Exception e) {
			filePath = "";
		}

		return filePath;
	}

	@Override
	public Response getUserGroupList() {
		Response response = new Response();
		List<UserGroup> userGroup = null;
		try {
			userGroup = (List<UserGroup>) userGroupR.findAll();
			response.setData(userGroup);
			response.setStatus(true);
			response.setMessage("Success");
			return response;
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("somthing went wrong");
			return response;
		}
	}

	@Override
	public Response getNavigationList(Long userGroupId) {
		List<NavigationMenuDTO> navigationMenuList = null;
		Response response = new Response();
		try {
			navigationMenuList = navigationR.getNavigationMenuByUserGroupId(userGroupId).stream()
					.map(menu -> new NavigationMenuDTO(  (Long) menu[0], 
					        (String) menu[1], 
					        (Boolean) menu[2], 
					        (Boolean) menu[3], 
					        (Boolean) menu[4]))
					.collect(Collectors.toList());
			response.setData(navigationMenuList);
			response.setStatus(true);
			response.setMessage("Success");

			return response;

		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("somthing went wrong");
			return response;
		}
	}

}
