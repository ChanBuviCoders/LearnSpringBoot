package config.serviceI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import config.DAO.UserGroupR;
import config.DTO.Response;
import config.Entity.UserGroup;
import config.Service.UserGroupService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupServiceI implements UserGroupService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupServiceI.class);

	private final UserGroupR userGroupR;

	@Override
	public Response getAllUserGroups() {
		Response response = new Response();
		try {
			response.setData(userGroupR.findAll());
			response.setStatus(true);
			response.setMessage("Success");
			return response;
		} catch (Exception e) {
			logger.error("getAllUserGroups failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response getUserGroupById(Long userGroupId) {
		Response response = new Response();
		try {
			return userGroupR.findById(userGroupId).map(group -> {
				Response ok = new Response();
				ok.setData(group);
				ok.setStatus(true);
				ok.setMessage("Success");
				return ok;
			}).orElseGet(() -> {
				Response notFound = new Response();
				notFound.setStatus(false);
				notFound.setMessage("User group not found");
				return notFound;
			});
		} catch (Exception e) {
			logger.error("getUserGroupById failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response createUserGroup(UserGroup userGroup) {
		Response response = new Response();
		try {
			UserGroup saved = userGroupR.save(userGroup);
			response.setData(saved);
			response.setStatus(true);
			response.setMessage("User group created successfully");
			logger.info("User group created: {}", saved.getUserGroupId());
			return response;
		} catch (Exception e) {
			logger.error("createUserGroup failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response updateUserGroup(UserGroup userGroup) {
		Response response = new Response();
		try {
			if (userGroup.getUserGroupId() == null || !userGroupR.existsById(userGroup.getUserGroupId())) {
				response.setStatus(false);
				response.setMessage("User group not found");
				return response;
			}
			UserGroup saved = userGroupR.save(userGroup);
			response.setData(saved);
			response.setStatus(true);
			response.setMessage("User group updated successfully");
			return response;
		} catch (Exception e) {
			logger.error("updateUserGroup failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response deleteUserGroup(Long userGroupId) {
		Response response = new Response();
		try {
			if (!userGroupR.existsById(userGroupId)) {
				response.setStatus(false);
				response.setMessage("User group not found");
				return response;
			}
			userGroupR.deleteById(userGroupId);
			response.setStatus(true);
			response.setMessage("User group deleted successfully");
			logger.info("User group deleted: {}", userGroupId);
			return response;
		} catch (Exception e) {
			logger.error("deleteUserGroup failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}
}
