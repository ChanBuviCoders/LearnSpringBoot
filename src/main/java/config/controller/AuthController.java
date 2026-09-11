package config.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.apiclub.captcha.Captcha;
import config.Capcha.CaptchaUtil;
import config.DTO.CapchaModel;
import config.DTO.Response;
import config.Entity.UserAccount;
import config.Service.AuthService;
import config.commonConfig.JsonWebToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, session and captcha APIs")
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	/** In-memory captcha store: captchaId -> answer (answer is never returned to client). */
	private static final ConcurrentHashMap<String, String> CAPTCHA_STORE = new ConcurrentHashMap<>();

	private final JsonWebToken jwtService;
	private final AuthService authService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@SecurityRequirements
	@Operation(summary = "Authenticate user and return JWT")
	@PostMapping(value = "/authSession", produces = "application/json")
	public Response authSession(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			if (userAccount.getUserName() != null && userAccount.getPassword() != null) {
				return authService.authSession(userAccount);
			}
			response.setStatus(false);
			response.setMessage("User Authentication Failure");
			return response;
		} catch (Exception e) {
			logger.error("authSession failed", e);
			response.setStatus(false);
			response.setMessage("User Authentication Failure");
			return response;
		}
	}

	@Operation(summary = "Resolve current session from JWT")
	@PostMapping(value = "/getSession", produces = "application/json")
	public Response getSession(@RequestBody String jwtToken) {
		Response response = new Response();
		try {
			if (jwtToken != null && !jwtToken.isBlank()) {
				return authService.getSession(jwtService.stripBearerPrefix(jwtToken.replace("\"", "")));
			}
			response.setStatus(false);
			response.setMessage("Token required");
			return response;
		} catch (Exception e) {
			logger.error("getSession failed", e);
			response.setStatus(false);
			response.setMessage("Failure");
			return response;
		}
	}

	@SecurityRequirements
	@Operation(summary = "Generate captcha image (answer stored server-side)")
	@PostMapping(value = "/getCapcha", produces = "application/json")
	public String getCapcha() {
		try {
			CapchaModel capchaModel = generateCapcha();
			Map<String, Object> json = new LinkedHashMap<>();
			json.put("captchaId", capchaModel.getCaptcha());
			json.put("image", capchaModel.getRealCaptcha());
			json.put("base64", capchaModel.getRealCaptcha());
			return objectMapper.writeValueAsString(json);
		} catch (Exception e) {
			logger.error("Captcha generation failed", e);
			return "{\"error\":\"Captcha generation failed\"}";
		}
	}

	private CapchaModel generateCapcha() {
		Captcha captcha = CaptchaUtil.createCaptcha(240, 70);
		String captchaId = UUID.randomUUID().toString();
		CAPTCHA_STORE.put(captchaId, captcha.getAnswer());

		CapchaModel capchaModel = new CapchaModel();
		capchaModel.setCaptcha(captchaId);
		capchaModel.setHiddenCaptcha(captcha.getAnswer());
		capchaModel.setRealCaptcha(CaptchaUtil.encodeCaptcha(captcha));
		return capchaModel;
	}

	/** Validate captcha answer if client sends captchaId + value (optional for login). */
	public static boolean validateCaptcha(String captchaId, String answer) {
		if (captchaId == null || answer == null) {
			return false;
		}
		String expected = CAPTCHA_STORE.remove(captchaId);
		return expected != null && expected.equalsIgnoreCase(answer.trim());
	}

	@Operation(summary = "Logout and update last login date")
	@PostMapping(value = "/logout", produces = "application/json")
	public Response logout(@RequestBody UserAccount userAccount) {
		Response response = new Response();
		try {
			return authService.logout(userAccount);
		} catch (Exception e) {
			logger.error("logout failed", e);
			response.setStatus(false);
			return response;
		}
	}
}
