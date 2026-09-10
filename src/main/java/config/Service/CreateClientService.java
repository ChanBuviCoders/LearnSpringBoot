package config.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import config.DTO.Response;
import config.Entity.UserAccount;

@Service
public interface CreateClientService {

	public Response createClient(MultipartFile adharFile, MultipartFile panFile, UserAccount userAccount);

	public Response getUserGroupList();

	public Response getNavigationList(Long userGroupId);

}
