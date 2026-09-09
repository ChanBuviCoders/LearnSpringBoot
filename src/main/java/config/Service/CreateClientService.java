package config.Service;

import org.springframework.stereotype.Service;

import config.DTO.Response;
import config.Entity.UserAccount;

@Service
public interface CreateClientService {

	public Response createClient(UserAccount userAccount);

	public Response getUserGroupList();

	public Response getNavigationList(Long userGroupId);
}
