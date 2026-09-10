package config.commonConfig;

import config.commonConfig.interceptors.JwtInterceptors;
import config.commonConfig.interceptors.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web configuration class for registering interceptors and CORS settings
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

	@Autowired
	JwtInterceptors jwtInterceptors;

	@Autowired
	HttpLoggingInterceptor httpLoggingInterceptor;

	// Inject the value from application.properties
	@Value("${app.cors.allowed-origins:*}")
	private String allowedOrigins;

	/**
	 * Register request/response interceptors
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(httpLoggingInterceptor).addPathPatterns("/api/**");
		registry.addInterceptor(jwtInterceptors);
		logger.info("Interceptors registered successfully");
	}

	/**
	 * Configure CORS settings for the application
	 */
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
						.allowedOrigins(allowedOrigins.split(","))
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true)
						.maxAge(3600);
				logger.info("CORS configuration applied for origins: {}", allowedOrigins);
			}
		};
	}

}
