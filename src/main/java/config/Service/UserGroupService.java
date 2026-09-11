package config.Service;

import config.DTO.Response;
import config.Entity.UserGroup;

public interface UserGroupService {

	Response getAllUserGroups();

	Response getUserGroupById(Long userGroupId);

	Response createUserGroup(UserGroup userGroup);

	Response updateUserGroup(UserGroup userGroup);

	Response deleteUserGroup(Long userGroupId);
}
