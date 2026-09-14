package config.financial.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class UserRoleAssignmentRulesTest {

	@Test
	void normalizeRoleIdsRejectsNullListAndNullEntriesAndDeduplicates() {
		assertThatThrownBy(() -> UserRoleAssignmentRules.normalizeRoleIds(null))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
				.isEqualTo(HttpStatus.BAD_REQUEST);

		List<Long> withNull = new ArrayList<>();
		withNull.add(1L);
		withNull.add(null);
		assertThatThrownBy(() -> UserRoleAssignmentRules.normalizeRoleIds(withNull))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("must not contain null");

		assertThat(UserRoleAssignmentRules.normalizeRoleIds(List.of(3L, 1L, 3L, 2L)))
				.containsExactly(3L, 1L, 2L);
	}

	@Test
	void adminCannotDropTheirOwnAdminRole() {
		assertThatThrownBy(() -> UserRoleAssignmentRules.rejectRemovingOwnAdminRole(true, true, false))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
				.isEqualTo(HttpStatus.CONFLICT);

		UserRoleAssignmentRules.rejectRemovingOwnAdminRole(true, true, true);
		UserRoleAssignmentRules.rejectRemovingOwnAdminRole(false, true, false);
		UserRoleAssignmentRules.rejectRemovingOwnAdminRole(true, false, false);
	}

	@Test
	void adminCannotDeactivateThemselves() {
		assertThatThrownBy(() -> UserRoleAssignmentRules.rejectSelfDeactivation(true, false))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("cannot deactivate");

		UserRoleAssignmentRules.rejectSelfDeactivation(true, true);
		UserRoleAssignmentRules.rejectSelfDeactivation(false, false);
	}
}
