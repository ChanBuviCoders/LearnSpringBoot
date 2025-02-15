package config.serviceI;

import org.springframework.stereotype.Service;

import config.DTO.response;
import config.Entity.userAccount;

@Service
public interface MultipleInhertanceTest {
	public response authSession(userAccount userAccount);
}
