package config.commonConfig;

import config.commonConfig.interceptors.JwtInterceptors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	public WebConfig() {
		System.out.println(
				"*******************************************************YOU CAN DO IT CHANDRAN SUBRAMANI*************************************************************");
	}

	@Autowired
	JwtInterceptors jwtInterceptors;

	// Inject the value from application.properties
	@Value("${app.cors.allowed-origins}")
	private String allowedOrigins;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// TODO Auto-generated method stub
		registry.addInterceptor(jwtInterceptors);
		WebMvcConfigurer.super.addInterceptors(registry);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**").allowedOrigins(allowedOrigins.split(",")) // Supports comma-separated
																							// origins
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowedHeaders("*")
						.allowCredentials(true);
			}
		};
	}

}
