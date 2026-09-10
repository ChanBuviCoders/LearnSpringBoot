package config.commonConfig.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP Logging Interceptor for logging incoming requests and outgoing responses
 */
@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(HttpLoggingInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		long startTime = System.currentTimeMillis();
		request.setAttribute("startTime", startTime);

		logger.info("=== REQUEST START ===");
		logger.info("Method: {} | URI: {}", request.getMethod(), request.getRequestURI());
		logger.info("Remote Address: {}", request.getRemoteAddr());
		if (request.getQueryString() != null) {
			logger.info("Query String: {}", request.getQueryString());
		}
		logger.info("=== REQUEST END ===");

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) throws Exception {
		long startTime = (long) request.getAttribute("startTime");
		long duration = System.currentTimeMillis() - startTime;

		logger.info("=== RESPONSE START ===");
		logger.info("Status: {} | Duration: {}ms", response.getStatus(), duration);
		logger.info("Content-Type: {}", response.getContentType());
		logger.info("=== RESPONSE END ===");

		if (ex != null) {
			logger.error("Request failed: ", ex);
		}
	}
}
