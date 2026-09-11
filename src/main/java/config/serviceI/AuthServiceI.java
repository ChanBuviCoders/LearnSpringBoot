package config.serviceI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

import config.DAO.UserAccountR;
import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.AuthService;
import config.Service.AzureBlobService;
import config.commonConfig.JsonWebToken;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceI implements AuthService {

	private final UserAccountR userAccountR;
	private final JsonWebToken jwtService;
	private final AzureBlobService azureBlobService;

	@Override
	public Response authSession(UserAccount userAccount) {
		Response response = new Response();
		try {
			UserAccount ua = userAccountR.findByUserName(userAccount.getUserName());
			if (ua != null) {
				if (ua.getLoginAttempt() < 3) {
					if (ua.getPassword().equals(userAccount.getPassword())) {
						ua.setActive(true);
						ua.setLoginAttempt(0);
						userAccountR.save(ua);
						response.setToken(jwtService.generateToken(ua));
						response.setStatus(true);
						response.setMessage("User Authentication Success");
					} else {
						ua.setActive(false);
						ua.setLoginAttempt(ua.getLoginAttempt() + 1);
						userAccountR.save(ua);
						response.setStatus(false);
						response.setMessage("Incorrect password, " + "you have only " + (3 - ua.getLoginAttempt())
								+ " more attempt" + (3 - ua.getLoginAttempt() > 1 ? "s" : ""));
						if (ua.getLoginAttempt() > 2)
							response.setMessage("Your account has been locked");
					}
				} else {
					response.setStatus(false);
					response.setMessage("Your account has been locked");
				}

				return response;
			} else {
				response.setStatus(false);
				response.setMessage("Invalid username or password");
				return response;
			}
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("User Authentication Failure");
			return response;
		}
	}

	public void updateLastLoginDate(long userAccountId) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-d");
		java.util.Date date = new java.util.Date();
		String lld = formatter.format(date);
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Connection conn = DriverManager.getConnection(
					"jdbc:sqlserver://localhost:1433;Database=springBoot;Trusted_Connection=True;encrypt=true;trustServerCertificate=true;");
			Statement Stmt = conn.createStatement();
			String Query = "update USER_ACCOUNT  set LAST_LOGIN_DATE=" + "'" + lld + "'" + " where USER_ACCOUNT_ID="
					+ userAccountId;
			Stmt.executeUpdate(Query);
			conn.close();

		} catch (Exception e) {
			System.out.println("ooooooooooo" + e);
		}

	}

	@Override
	public Response getSession(String jwtToken) {
		Response response = new Response();
		try {
			String[] parts = jwtToken.split("\\.");
			Base64.Decoder decoder = Base64.getUrlDecoder();
			String payload = new String(decoder.decode(parts[1]));

			// Parse JSON payload using Gson JsonParser
			com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
			Long userAccountId = jsonObject.get("userAccountId").getAsLong();

			return getTokenUser(userAccountId, jwtToken);

		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("Failure");
			response.setError("Somthing Went Wrong");
			return response;
		}
	}

	public Response getTokenUser(Long userAccountId, String jwtToken) {
		Response response = new Response();

		try {
			if (userAccountId != null) {
				UserAccount cudb = userAccountR.findByUserAccountId(userAccountId);
				cudb.setPanImagePath(azureBlobService.generateReadToken(cudb.getPanImagePath()));
				cudb.setAdharImagePath(azureBlobService.generateReadToken(cudb.getAdharImagePath()));
				response.setStatus(true);
				response.setMessage("success");
				response.setData(cudb);
				response.setToken(jwtToken);
				return response;
			} else {
				response.setStatus(false);
				response.setMessage(jwtToken);
				return response;
			}
		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("Failure");
			response.setError("Somthing Went Wrong");
			return response;
		}
	}

	public String fileToBase64(String filePath) throws IOException {
		Path path = Paths.get(filePath);

		if (!Files.exists(path) || !Files.isRegularFile(path)) {
			return null;
		}

		return Base64.getEncoder().encodeToString(Files.readAllBytes(path));
	}

	@Override
	public Response logout(UserAccount userAccount) {
		Response response = new Response();

		try {
			UserAccount userAccountFr = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			String fDate = formatter.format(date);
			java.util.Date dateType = formatter.parse(fDate); // Returns a Date format object with the pattern
			java.sql.Date sqlStartDate = new java.sql.Date(dateType.getTime());
			userAccountFr.setLastLoginDate(sqlStartDate);
			userAccountR.save(userAccountFr);
			response.setStatus(true);
		} catch (Exception e) {
			// TODO: handle exception
			response.setStatus(false);
		}

		return response;
	}

}
