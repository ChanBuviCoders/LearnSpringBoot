package config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.apiclub.captcha.Captcha;
import config.Capcha.CaptchaUtil;
import config.DTO.CapchaModel;
import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.AuthService;
import config.commonConfig.JsonWebToken;

@RestController
@RequestMapping(path = "/api")
public class AuthController {

	@Autowired
	JsonWebToken jwtService;

	@Autowired
	AuthService authService;

	@RequestMapping(value = "/authSession", method = RequestMethod.POST, produces = "application/json")
	public Response authSession(@RequestBody UserAccount userAccount) {
		Response response = new Response();

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

	@RequestMapping(value = "/getSession", method = RequestMethod.POST, produces = "application/Json")
	public Response getSession(@RequestBody String jwttoken) {
		Response response = new Response();
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
		try {
			CapchaModel capchaModel = generateCapcha();
			Map<String, Object> json = new LinkedHashMap<>();
			json.put("value", capchaModel.getHiddenCaptcha());
			json.put("image", capchaModel.getRealCaptcha());
			json.put("base64", capchaModel.getCaptcha());
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(json);
		} catch (Exception e) {
			Map<String, Object> error = new LinkedHashMap<>();
			error.put("error", "Captcha generation failed");
			try {
				return new ObjectMapper().writeValueAsString(error);
			} catch (Exception ex) {
				return "{\"error\":\"Captcha generation failed\"}";
			}
		}
	}

	private CapchaModel generateCapcha() {

		Captcha captcha = CaptchaUtil.createCaptcha(240, 70);
		CapchaModel capchaModel = new CapchaModel();
		capchaModel.setHiddenCaptcha(captcha.getAnswer());

		capchaModel.setRealCaptcha(CaptchaUtil.encodeCaptcha(captcha));
		return capchaModel;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST, produces = "application/Json")
	public Response logout(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			return authService.logout(userAccount);
		} catch (Exception e) {
			response.setStatus(false);
			return response;
		}
	}
}
