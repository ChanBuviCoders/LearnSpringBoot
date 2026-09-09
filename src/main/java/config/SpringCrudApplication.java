package config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

import config.controller.AuthController;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class SpringCrudApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SpringCrudApplication.class);
	}

	public static void main(String[] args) {
		ApplicationContext container = SpringApplication.run(SpringCrudApplication.class, args);
		AuthController ac = container.getBean(AuthController.class);
		ac.getCapcha();

	}

}
