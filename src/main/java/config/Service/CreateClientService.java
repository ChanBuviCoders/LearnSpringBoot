package config.Service;

import org.springframework.web.multipart.MultipartFile;

import config.DTO.Response;
import config.Entity.UserAccount;

public interface CreateClientService {

	Response createClient(MultipartFile adharFile, MultipartFile panFile, UserAccount userAccount);

	Response getUserGroupList();

	Response getNavigationList(Long userGroupId);
}
