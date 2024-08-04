package config.commonConfig.interceptors;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import config.commonConfig.jsonWebToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class jwtInterceptors extends WebRequestHandlerInterceptorAdapter {

	public jwtInterceptors(WebRequestInterceptor requestInterceptor) {
		super(requestInterceptor);
	}

	@Autowired
	jsonWebToken jwtService;

	private static String[] pubicApis = { "getCapcha", "authSession" };

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		try {
			String[] api = request.getRequestURI().split("\\/");

			String authToken = request.getHeader("Authorization");

			if (authToken != null) {
				if (!Arrays.asList(pubicApis).contains(api[api.length - 1]))
					jwtService.verifyToken(authToken);
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("---validate token---" + e);
			response.sendError(504, "full authentication error");
		}

		return super.preHandle(request, response, handler);
	}
}
