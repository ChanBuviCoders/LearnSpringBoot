package config.serviceI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import config.DAO.userAccountR;
import config.DTO.response;
import config.Entity.userAccount;
import config.Service.authService;
import config.commonConfig.jsonWebToken;

@Service
public class authServiceI implements authService {

	@Autowired
	userAccountR userAccountR;

	@Autowired
	jsonWebToken jwtService;

	@Override
	public response authSession(userAccount userAccount) {
		response response = new response();
		try {
			userAccount ua = userAccountR.findByUserNameAndPassword(userAccount.getUserName(),
					userAccount.getPassword());
			if (ua != null) {
				ua.setActive(true);
				userAccountR.save(ua);
				response.setToken(jwtService.generateToken(ua));
				response.setStatus(true);
				response.setMessage("User Authentication Success");
				return response;
			} else {
				response.setStatus(false);
				response.setMessage("User Authentication Failure");
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
	public response getSession(String jwtToken) {
		response response = new response();
		try {
			String[] parts = jwtToken.split("\\.");
			Base64.Decoder decoder = Base64.getUrlDecoder();
			String payload = new String(decoder.decode(parts[1]));
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(payload.toString());
			return getTokenUser((Long) json.get("userAccountId"), jwtToken);

		} catch (Exception e) {
			response.setStatus(false);
			response.setMessage("Failure");
			response.setError("Somthing Went Wrong");
			return response;
		}
	}

	public response getTokenUser(Long userAccountId, String jwtToken) {
		response response = new response();

		try {
			if (userAccountId != null) {
				userAccount cudb = userAccountR.findByUserAccountId(userAccountId);
				cudb.setPanImagePath(fileToBase64(cudb.getPanImagePath()));
				cudb.setAdharImagePath(fileToBase64(cudb.getAdharImagePath()));
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

	public String fileToBase64(String filepath) throws IOException {
		byte[] byteData = Files.readAllBytes(Paths.get(filepath));
		String base64String = Base64.getEncoder().encodeToString(byteData);
		return base64String;
	}

	@Override
	public response logout(userAccount userAccount) {
		response response = new response();

		try {
			userAccount userAccountFr = userAccountR.findByUserAccountId(userAccount.getUserAccountId());
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
