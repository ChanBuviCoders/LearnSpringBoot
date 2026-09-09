package config.commonConfig;

import config.commonConfig.interceptors.JwtInterceptors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// TODO Auto-generated method stub
		registry.addInterceptor(jwtInterceptors);
		WebMvcConfigurer.super.addInterceptors(registry);
	}

}
