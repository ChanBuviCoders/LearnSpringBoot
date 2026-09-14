package config.financial.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import config.DAO.UserAccountR;
import config.DTO.Response;
import config.Entity.UserAccount;
import config.commonConfig.ResponseBuilder;
import config.financial.audit.AuditAction;
import config.financial.audit.AuditService;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Admin user and role assignment against existing {@code finance} RBAC tables.
 * Legacy {@link UserAccount} mapping is left unchanged; assignments store the
 * user id as a scalar foreign key.
 */
public final class UserRoleManagementModule {
	private UserRoleManagementModule() {
	}
}

@Entity
@Table(name = "permissions", schema = "finance")
@Getter
@Setter
class FinancePermission {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "permission_code", nullable = false, unique = true, length = 80)
	private String permissionCode;
	@Column(length = 300)
	private String description;
}

@Entity
@Table(name = "roles", schema = "finance")
@Getter
@Setter
class FinanceRole {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "role_code", nullable = false, unique = true, length = 40)
	private String roleCode;
	@Column(name = "role_name", nullable = false, length = 100)
	private String roleName;
	@Column(length = 300)
	private String description;
	@Column(nullable = false)
	private boolean active = true;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "role_permissions", schema = "finance",
			joinColumns = @JoinColumn(name = "role_id"),
			inverseJoinColumns = @JoinColumn(name = "permission_id"))
	private Set<FinancePermission> permissions = new LinkedHashSet<>();
}

class UserRoleAssignmentId implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long userAccountId;
	private Long roleId;

	UserRoleAssignmentId() {
	}

	UserRoleAssignmentId(Long userAccountId, Long roleId) {
		this.userAccountId = userAccountId;
		this.roleId = roleId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UserRoleAssignmentId that)) {
			return false;
		}
		return Objects.equals(userAccountId, that.userAccountId) && Objects.equals(roleId, that.roleId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userAccountId, roleId);
	}
}

@Entity
@Table(name = "user_roles", schema = "finance")
@IdClass(UserRoleAssignmentId.class)
@Getter
@Setter
class UserRoleAssignment {
	@Id
	@Column(name = "user_account_id", nullable = false)
	private Long userAccountId;
	@Id
	@Column(name = "role_id", nullable = false)
	private Long roleId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "role_id", insertable = false, updatable = false)
	private FinanceRole role;
}

interface FinanceRoleRepository extends JpaRepository<FinanceRole, Long> {
	@EntityGraph(attributePaths = "permissions")
	List<FinanceRole> findAllByOrderByRoleCodeAsc();
}

interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UserRoleAssignmentId> {
	@Query("""
			select ur from UserRoleAssignment ur
			join fetch ur.role
			where ur.userAccountId in :userIds
			""")
	List<UserRoleAssignment> findWithRoleByUserAccountIdIn(@Param("userIds") Collection<Long> userIds);

	@Query("""
			select ur from UserRoleAssignment ur
			join fetch ur.role
			where ur.userAccountId = :userId
			""")
	List<UserRoleAssignment> findWithRoleByUserAccountId(@Param("userId") Long userId);

	void deleteByUserAccountId(Long userAccountId);
}

record AssignedRoleSummary(Long id, String roleCode, String roleName, boolean active) {
	static AssignedRoleSummary from(FinanceRole role) {
		return new AssignedRoleSummary(role.getId(), role.getRoleCode(), role.getRoleName(), role.isActive());
	}
}

record AdminUserSummary(Long userId, String userName, String firstName, String lastName, String email,
		boolean active, Long userGroupId, List<AssignedRoleSummary> roles) {
	static AdminUserSummary from(UserAccount account, List<AssignedRoleSummary> roles) {
		return new AdminUserSummary(account.getUserAccountId(), account.getUserName(), account.getFirstName(),
				account.getLastName(), account.getEmail(), account.isActive(), account.getUserGroupId(), roles);
	}
}

record PermissionSummary(Long id, String permissionCode, String description) {
	static PermissionSummary from(FinancePermission permission) {
		return new PermissionSummary(permission.getId(), permission.getPermissionCode(), permission.getDescription());
	}
}

record RoleWithPermissions(Long id, String roleCode, String roleName, String description, boolean active,
		List<PermissionSummary> permissions) {
	static RoleWithPermissions from(FinanceRole role) {
		List<PermissionSummary> permissions = role.getPermissions().stream()
				.map(PermissionSummary::from)
				.sorted((left, right) -> left.permissionCode().compareToIgnoreCase(right.permissionCode()))
				.toList();
		return new RoleWithPermissions(role.getId(), role.getRoleCode(), role.getRoleName(), role.getDescription(),
				role.isActive(), permissions);
	}
}

record AssignRolesRequest(@NotNull List<Long> roleIds) {
}

record UpdateUserStatusRequest(@NotNull Boolean active) {
}

final class UserRoleAssignmentRules {
	static final String ADMIN_ROLE_CODE = "ADMIN";

	private UserRoleAssignmentRules() {
	}

	static LinkedHashSet<Long> normalizeRoleIds(List<Long> roleIds) {
		if (roleIds == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roleIds is required");
		}
		LinkedHashSet<Long> unique = new LinkedHashSet<>();
		for (Long roleId : roleIds) {
			if (roleId == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roleIds must not contain null");
			}
			unique.add(roleId);
		}
		return unique;
	}

	static void rejectRemovingOwnAdminRole(boolean actingOnSelf, boolean currentlyHasAdmin, boolean remainsAdmin) {
		if (actingOnSelf && currentlyHasAdmin && !remainsAdmin) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"You cannot remove your own ADMIN role");
		}
	}

	static void rejectSelfDeactivation(boolean actingOnSelf, boolean active) {
		if (actingOnSelf && !active) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot deactivate your own account");
		}
	}

	static boolean hasAdminRole(Collection<FinanceRole> roles) {
		return roles.stream().anyMatch(role -> ADMIN_ROLE_CODE.equalsIgnoreCase(role.getRoleCode()));
	}
}

@Service
@RequiredArgsConstructor
class AdminUserService {
	private final UserAccountR userAccounts;
	private final FinanceRoleRepository roles;
	private final UserRoleAssignmentRepository assignments;
	private final AuditService auditService;

	@Transactional(readOnly = true)
	List<AdminUserSummary> listUsers() {
		List<UserAccount> accounts = userAccounts.findAll();
		if (accounts.isEmpty()) {
			return List.of();
		}
		List<Long> userIds = accounts.stream().map(UserAccount::getUserAccountId).toList();
		Map<Long, List<AssignedRoleSummary>> rolesByUser = new LinkedHashMap<>();
		for (UserRoleAssignment assignment : assignments.findWithRoleByUserAccountIdIn(userIds)) {
			rolesByUser.computeIfAbsent(assignment.getUserAccountId(), unused -> new ArrayList<>())
					.add(AssignedRoleSummary.from(assignment.getRole()));
		}
		return accounts.stream()
				.map(account -> AdminUserSummary.from(account,
						List.copyOf(rolesByUser.getOrDefault(account.getUserAccountId(), List.of()))))
				.toList();
	}

	@Transactional(readOnly = true)
	List<RoleWithPermissions> listRoles() {
		return roles.findAllByOrderByRoleCodeAsc().stream().map(RoleWithPermissions::from).toList();
	}

	@Transactional
	AdminUserSummary replaceRoles(Long userId, AssignRolesRequest request) {
		UserAccount target = requiredUser(userId);
		LinkedHashSet<Long> requestedIds = UserRoleAssignmentRules.normalizeRoleIds(request.roleIds());
		List<FinanceRole> selected = loadRoles(requestedIds);
		List<UserRoleAssignment> current = assignments.findWithRoleByUserAccountId(userId);
		boolean currentlyHasAdmin = UserRoleAssignmentRules.hasAdminRole(
				current.stream().map(UserRoleAssignment::getRole).toList());
		UserRoleAssignmentRules.rejectRemovingOwnAdminRole(
				isSelf(target), currentlyHasAdmin, UserRoleAssignmentRules.hasAdminRole(selected));

		List<Long> previousIds = current.stream().map(UserRoleAssignment::getRoleId).toList();
		assignments.deleteByUserAccountId(userId);
		assignments.flush();
		for (FinanceRole role : selected) {
			UserRoleAssignment assignment = new UserRoleAssignment();
			assignment.setUserAccountId(userId);
			assignment.setRoleId(role.getId());
			assignment.setRole(role);
			assignments.save(assignment);
		}
		auditService.record("UserAccount", userId, AuditAction.UPDATE,
				Map.of("roleIds", previousIds),
				Map.of("roleIds", new ArrayList<>(requestedIds)));
		return AdminUserSummary.from(target, selected.stream().map(AssignedRoleSummary::from).toList());
	}

	@Transactional
	AdminUserSummary updateStatus(Long userId, UpdateUserStatusRequest request) {
		UserAccount target = requiredUser(userId);
		boolean active = request.active();
		UserRoleAssignmentRules.rejectSelfDeactivation(isSelf(target), active);
		boolean previous = target.isActive();
		target.setActive(active);
		UserAccount saved = userAccounts.save(target);
		auditService.record("UserAccount", userId, AuditAction.STATUS_CHANGE,
				Map.of("active", previous), Map.of("active", saved.isActive()));
		List<AssignedRoleSummary> roleSummaries = assignments.findWithRoleByUserAccountId(userId).stream()
				.map(assignment -> AssignedRoleSummary.from(assignment.getRole()))
				.toList();
		return AdminUserSummary.from(saved, roleSummaries);
	}

	private List<FinanceRole> loadRoles(LinkedHashSet<Long> requestedIds) {
		if (requestedIds.isEmpty()) {
			return List.of();
		}
		Map<Long, FinanceRole> found = roles.findAllById(requestedIds).stream()
				.collect(Collectors.toMap(FinanceRole::getId, role -> role));
		List<Long> missing = requestedIds.stream().filter(id -> !found.containsKey(id)).toList();
		if (!missing.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role ids: " + missing);
		}
		List<FinanceRole> selected = requestedIds.stream().map(found::get).toList();
		List<String> inactive = selected.stream()
				.filter(role -> !role.isActive())
				.map(FinanceRole::getRoleCode)
				.toList();
		if (!inactive.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inactive roles cannot be assigned: " + inactive);
		}
		return selected;
	}

	private UserAccount requiredUser(Long userId) {
		UserAccount account = userAccounts.findByUserAccountId(userId);
		if (account == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		return account;
	}

	private boolean isSelf(UserAccount target) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			return false;
		}
		UserAccount actor = userAccounts.findByUserName(authentication.getName());
		return actor != null && Objects.equals(actor.getUserAccountId(), target.getUserAccountId());
	}
}

@RestController
@RequestMapping("/api/v2/admin")
@RequiredArgsConstructor
class AdminUserController {
	private final AdminUserService service;

	@GetMapping("/users")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER_1')")
	ResponseEntity<Response> listUsers() {
		return ResponseEntity.ok(ResponseBuilder.success(service.listUsers()));
	}

	@GetMapping("/roles")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER_1')")
	ResponseEntity<Response> listRoles() {
		return ResponseEntity.ok(ResponseBuilder.success(service.listRoles()));
	}

	@PutMapping("/users/{userId}/roles")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER_1')")
	ResponseEntity<Response> replaceRoles(
			@PathVariable Long userId,
			@Valid @RequestBody AssignRolesRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.replaceRoles(userId, request), "Roles updated"));
	}

	@PatchMapping("/users/{userId}/status")
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER_1')")
	ResponseEntity<Response> updateStatus(
			@PathVariable Long userId,
			@Valid @RequestBody UpdateUserStatusRequest request) {
		return ResponseEntity.ok(ResponseBuilder.success(service.updateStatus(userId, request), "Status updated"));
	}
}

record StaffMember(Long userId, String userName, String firstName, String lastName) {
	static StaffMember from(UserAccount account) {
		return new StaffMember(account.getUserAccountId(), account.getUserName(),
				account.getFirstName(), account.getLastName());
	}
}

@RestController
@RequestMapping("/api/v2/staff")
@RequiredArgsConstructor
class StaffDirectoryController {
	private final UserAccountR userAccounts;

	@GetMapping
	@PreAuthorize("hasAnyAuthority('ROLE_VIEWER','ROLE_COLLECTOR','ROLE_ACCOUNTANT','ROLE_MANAGER','ROLE_ADMIN','ROLE_USER_1')")
	ResponseEntity<Response> list() {
		List<StaffMember> staff = userAccounts.findAll().stream()
				.filter(UserAccount::isActive)
				.map(StaffMember::from)
				.toList();
		return ResponseEntity.ok(ResponseBuilder.success(staff));
	}
}
