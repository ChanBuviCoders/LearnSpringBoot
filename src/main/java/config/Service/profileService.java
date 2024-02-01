package config.Service;

import org.springframework.stereotype.Service;

import config.DTO.checkCurrentPwd;
import config.DTO.response;
import config.Entity.userAccount;

@Service
public interface profileService {

	String getUserProfile(userAccount userAccount);

	response updateUserProfile(userAccount userAccount);

	response changePassword(userAccount userAccount);

	response checkCurrentPassword(userAccount userAccount);

}
