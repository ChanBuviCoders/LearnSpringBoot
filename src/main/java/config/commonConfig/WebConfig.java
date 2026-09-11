package config.commonConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import config.commonConfig.interceptors.HttpLoggingInterceptor;

/**
 * Web MVC configuration — CORS is handled by Spring Security.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

	@Autowired
	private HttpLoggingInterceptor httpLoggingInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(httpLoggingInterceptor).addPathPatterns("/api/**");
		logger.info("HTTP logging interceptor registered for /api/**");
	}
}
