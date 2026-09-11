package config.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

		return User.builder()
				.username(account.getUserName())
				.password(account.getPassword() != null ? account.getPassword() : "")
				.authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
				.accountLocked(!enabled)
				.disabled(!account.isActive() && account.getLoginAttempt() != null && account.getLoginAttempt() >= 3)
				.build();
	}
}
