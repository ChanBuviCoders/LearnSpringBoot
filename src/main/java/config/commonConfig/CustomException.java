package config.commonConfig;

import org.springframework.stereotype.Component;

@SuppressWarnings("serial")
@Component
public class CustomException extends Exception {
	CustomException() {

	}

	public CustomException(String msg) {
		super(msg);
	}
}
