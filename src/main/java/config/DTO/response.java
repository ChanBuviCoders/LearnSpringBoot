package config.DTO;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.NonNull;

@Data
@Component
public class response {

	private boolean Status;
	private String Message;
	private Object data = null;
	@NonNull
	private Object error;
	@NonNull
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isStatus() {
		return Status;
	}

	public void setStatus(boolean status) {
		Status = status;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Object getError() {
		return error;
	}

	public void setError(Object error) {
		this.error = error;
	}

}
