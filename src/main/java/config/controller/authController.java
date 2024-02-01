package config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

import cn.apiclub.captcha.Captcha;
import config.Capcha.CaptchaUtil;
import config.DTO.capchaModel;
import config.DTO.response;
import config.Entity.userAccount;
import config.Service.authService;
import config.commonConfig.jsonWebToken;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/api")
public class authController {

	@Autowired
	jsonWebToken jwtService;

	@Autowired
	response response;

	@Autowired
	authService authService;

	@RequestMapping(value = "/authSession", method = RequestMethod.POST, produces = "application/json")
	public response authSession(@RequestBody userAccount userAccount) {

		try {
			if (userAccount.getUserName() != null && userAccount.getPassword() != null) {
				return authService.authSession(userAccount);
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

	// @RequestHeader (name="Authorization") String jwttoken
//	 @SuppressWarnings("null")
	@RequestMapping(value = "/getSession", method = RequestMethod.POST, produces = "application/Json")
	public response getSession(@RequestBody String jwttoken) {
		try {
			if (!jwttoken.isEmpty()) {

				return authService.getSession(jwttoken);
			} else
				return response;
		} catch (Exception e) {
			return response;
		}
	}

	/**************************** Generate Capcha ********************************/
	@RequestMapping(value = "/getCapcha", method = RequestMethod.POST, produces = "application/Json")
	public String getCapcha() {
		JsonObject json = new JsonObject();
		try {
			capchaModel capchaModel = generateCapcha();
			json.addProperty("value", capchaModel.getHiddenCaptcha());
			json.addProperty("image", capchaModel.getRealCaptcha());
			json.addProperty("base64", capchaModel.getCaptcha());
		} catch (Exception e) {
			System.out.println(e);
		}

		return json.toString();
	}

	private capchaModel generateCapcha() {

		Captcha captcha = CaptchaUtil.createCaptcha(240, 70);
		capchaModel capchaModel = new capchaModel();
		capchaModel.setHiddenCaptcha(captcha.getAnswer());

		capchaModel.setRealCaptcha(CaptchaUtil.encodeCaptcha(captcha));
		return capchaModel;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST, produces = "application/Json")
	public response logout(@RequestBody userAccount userAccount) {
		try {
			return authService.logout(userAccount);
		} catch (Exception e) {
			response.setStatus(false);
			return response;
		}
	}
}
