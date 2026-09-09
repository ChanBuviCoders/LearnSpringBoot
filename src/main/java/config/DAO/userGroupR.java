package config.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import config.Entity.UserGroup;

public interface UserGroupR extends JpaRepository<UserGroup, Long> {

}
