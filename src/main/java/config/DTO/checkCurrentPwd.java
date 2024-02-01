package config.DTO;

import lombok.Data;

@Data
public class checkCurrentPwd {

	private long userAccountId;
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getUserAccountId() {
		return userAccountId;
	}

	public void setUserAccountId(long userAccountId) {
		this.userAccountId = userAccountId;
	}

	@Override
	public String toString() {
		return "checkCurrentPwd [userAccountId=" + userAccountId + ", password=" + password + "]";
	}

}