package config.Service;

import org.springframework.stereotype.Service;

import config.DTO.Response;
import config.Entity.UserAccount;

@Service
public interface ProfileService {

	String getUserProfile(UserAccount userAccount);

	Response updateUserProfile(UserAccount userAccount);

	Response changePassword(UserAccount userAccount);

	Response checkCurrentPassword(UserAccount userAccount);

}
