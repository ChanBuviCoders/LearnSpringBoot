package config.security;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;

import config.DAO.UserAccountR;
import config.Entity.UserAccount;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserAccountR userAccountRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserAccount account = userAccountRepository.findByUserName(username);
		if (account == null) {
			throw new UsernameNotFoundException("User not found: " + username);
		}

		String role = account.getUserGroupId() != null ? "ROLE_USER_" + account.getUserGroupId() : "ROLE_USER";
		boolean enabled = account.getLoginAttempt() == null || account.getLoginAttempt() < 3;
		Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
		authorities.add(new SimpleGrantedAuthority(role));
		try {
			userAccountRepository.findFinanceAuthorities(account.getUserAccountId()).stream()
					.map(SimpleGrantedAuthority::new)
					.forEach(authorities::add);
		} catch (DataAccessException ignored) {
			// Finance RBAC may not be migrated yet; retain legacy group authentication.
		}

		return User.builder()
				.username(account.getUserName())
				.password(account.getPassword() != null ? account.getPassword() : "")
				.authorities(authorities)
				.accountLocked(!enabled)
				.disabled(!account.isActive() && account.getLoginAttempt() != null && account.getLoginAttempt() >= 3)
				.build();
	}
}
