package config.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import config.Entity.userGroup;

public interface userGroupR extends JpaRepository<userGroup, Long> {

}
