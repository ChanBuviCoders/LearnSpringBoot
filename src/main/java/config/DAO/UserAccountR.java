package config.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import config.Entity.UserAccount;

@Repository
public interface UserAccountR extends JpaRepository<UserAccount, Long> {

	UserAccount getUserAccountByUserName(String userName);

	UserAccount findByUserNameAndPassword(String userName, String password);

	UserAccount findByUserAccountId(Long userAccountId);

	UserAccount findByUserName(String userName);

	@Query(value = """
			SELECT authority FROM (
				SELECT CONCAT('ROLE_', r.role_code) authority
				FROM finance.user_roles ur
				JOIN finance.roles r ON r.id = ur.role_id AND r.active = 1
				WHERE ur.user_account_id = :userId
				UNION
				SELECT p.permission_code authority
				FROM finance.user_roles ur
				JOIN finance.roles r ON r.id = ur.role_id AND r.active = 1
				JOIN finance.role_permissions rp ON rp.role_id = r.id
				JOIN finance.permissions p ON p.id = rp.permission_id
				WHERE ur.user_account_id = :userId
			) authorities
			""", nativeQuery = true)
	List<String> findFinanceAuthorities(@Param("userId") Long userId);

}
