package config.DTO;

import jakarta.persistence.Transient;
import lombok.Data;

@Data
public class capchaModel {
	private Integer id;
	private String name;
	private String email;

	@Transient
	private String captcha;

	@Transient
	private String hiddenCaptcha;

	@Transient
	private String realCaptcha;

}
