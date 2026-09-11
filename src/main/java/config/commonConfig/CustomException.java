package config.commonConfig;

@SuppressWarnings("serial")
public class CustomException extends Exception {

	public CustomException() {
	}

	public CustomException(String msg) {
		super(msg);
	}
}
