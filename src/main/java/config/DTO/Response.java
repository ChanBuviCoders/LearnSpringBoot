package config.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Response {

	private boolean status;
	private String message;
	private Object data;
	private Object error;
	private String token;
}
