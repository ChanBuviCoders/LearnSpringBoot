package config.serviceI;

import org.springframework.stereotype.Service;

import config.DTO.Response;
import config.Entity.UserAccount;

@Service
public interface MultipleInhertanceTest {
	public Response authSession(UserAccount userAccount);
}
