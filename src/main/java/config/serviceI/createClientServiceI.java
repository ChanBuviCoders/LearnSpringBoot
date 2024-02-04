package config.serviceI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import config.DAO.userAccountR;
import config.DTO.response;
import config.Entity.userAccount;
import config.Service.createClientService;

@Service
public class createClientServiceI implements createClientService {

	@Autowired
	userAccountR userAccountR;

	@SuppressWarnings("unused")
	@Override
	public response createClient(userAccount userAccount) {
		response response = new response();
		try {
			
			userAccount uA = userAccountR.getUserAccountByUserName(userAccount.getUserName());
			if (uA == null) {
				if (userAccount != null) {
					userAccount.setAdharImagePath(
							base64ToFile(userAccount.getAdharImagePath(), userAccount.getMobileNumber(), "adharImage"));
					userAccount.setPanImagePath(
							base64ToFile(userAccount.getPanImagePath(), userAccount.getMobileNumber(), "panImage"));
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

	String clientDocumentsPath = "G:\\chandran\\java\\springCrud\\documents\\clientDocuments";

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

}
