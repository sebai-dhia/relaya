package com.relaya.demo.config.security;

import com.relaya.demo.user.SpringDataUserRepository;
import com.relaya.demo.user.UserJpaEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class RelayaUserDetailsService implements UserDetailsService {

	private final SpringDataUserRepository userRepository;

	public RelayaUserDetailsService(SpringDataUserRepository userRepository) {
		this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<UserJpaEntity> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			throw new UsernameNotFoundException("User not found with email: " + email);
		}
		UserJpaEntity user = userOpt.get();
		return new UserPrincipal(
				user.getId(),
				user.getTenantId(),
				user.getEmail(),
				user.getPasswordHash(),
				user.getRole(),
				user.isActive()
		);
	}
}