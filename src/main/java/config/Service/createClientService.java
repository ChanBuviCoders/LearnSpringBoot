package config.Service;

import org.springframework.stereotype.Service;

import config.DTO.response;
import config.Entity.userAccount;

@Service
public interface createClientService {

	public response createClient(userAccount userAccount);
}
