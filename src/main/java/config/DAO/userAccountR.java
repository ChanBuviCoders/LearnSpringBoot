package config.DAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import config.Entity.userAccount;

@Repository
public interface userAccountR extends JpaRepository<userAccount, Long> {

	userAccount getUserAccountByUserName(String userName);
    
	userAccount findByUserNameAndPassword(String userName,String password);

	userAccount findByUserAccountId(Long userAccountId);

	userAccount findByUserAccountId(long userAccountId);

}
