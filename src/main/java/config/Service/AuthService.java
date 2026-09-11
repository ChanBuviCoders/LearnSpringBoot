package config.Service;

import config.DTO.Response;
import config.Entity.UserAccount;

public interface AuthService {

	Response authSession(UserAccount userAccount);

	Response getSession(String jwtToken);

	Response logout(UserAccount userAccount);
}
