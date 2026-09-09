package config.DTO;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@Component
@JsonInclude(Include.NON_NULL)
public class Response {

	private boolean Status;
	private String Message;
	private Object data;
	private Object error;
	private String token;

}
