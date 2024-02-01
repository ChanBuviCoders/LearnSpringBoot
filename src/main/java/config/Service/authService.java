package config.Service;

import org.springframework.stereotype.Service;

import config.DTO.response;
import config.Entity.userAccount;

@Service
public interface authService {

	public response authSession(userAccount userAccount);

	public response getSession(String jwtToken);

	public response logout(userAccount userAccount);
}
