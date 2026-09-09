package config.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import config.Entity.UserAccount;

@Repository
public interface UserAccountR extends JpaRepository<UserAccount, Long> {

	UserAccount getUserAccountByUserName(String userName);

	UserAccount findByUserNameAndPassword(String userName, String password);

	UserAccount findByUserAccountId(Long userAccountId);

	UserAccount findByUserAccountId(long userAccountId);

	UserAccount findByUserName(String userName);

}
