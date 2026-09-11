package config.Service;

import config.DTO.Response;
import config.Entity.UserAccount;

public interface ProfileService {

	Response getUserProfile(UserAccount userAccount);

	Response updateUserProfile(UserAccount userAccount);

	Response changePassword(UserAccount userAccount);

	Response checkCurrentPassword(UserAccount userAccount);
}
