package config.Service;

import org.springframework.stereotype.Service;

import config.DTO.Response;
import config.Entity.UserAccount;

@Service
public interface AuthService {

	public Response authSession(UserAccount userAccount);

	public Response getSession(String jwtToken);

	public Response logout(UserAccount userAccount);
}
