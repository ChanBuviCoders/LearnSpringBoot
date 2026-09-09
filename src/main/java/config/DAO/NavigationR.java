package config.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import config.Entity.NavigationMenu;

public interface NavigationR extends JpaRepository<NavigationMenu, Long> {
	@Query(value = "select nm.menuId,nm.menuName,up.isView,up.isEdit,up.isDelete from NavigationMenu as nm "
			+ " join UserPrivilegeMapping as upm on upm.menuId=nm.menuId"
			+ " join UserPrivilege as up on up.privilegeId=upm.privilegeId"
			+ " where upm.userGroupId=:userGroupId", nativeQuery = true)
	List<Object[]> getNavigationMenuByUserGroupId(@Param("userGroupId") Long userGroupId);

}
