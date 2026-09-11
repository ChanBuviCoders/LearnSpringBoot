package config.Service;

import config.DTO.Response;
import config.Entity.UserAccount;

public interface UserAccountService {

	Response getAllUsers();

	Response getUserById(Long userAccountId);

	Response updateUser(UserAccount userAccount);

	Response deleteUser(Long userAccountId);

	Response sendMail(String to, String subject, String text);
}
