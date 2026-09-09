package config.DTO;

import lombok.Data;

@Data
public class CheckCurrentPwd {

	private long userAccountId;
	private String password;

}