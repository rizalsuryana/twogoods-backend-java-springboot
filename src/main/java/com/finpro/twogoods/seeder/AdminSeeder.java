package com.finpro.twogoods.seeder;

import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${admin.email}")
	private String ADMIN_USERNAME;

	@Value("${admin.password}")
	private String ADMIN_PASSWORD;

	@Value("${admin.fullname}")
	private String ADMIN_FULLNAME;

	@Override
	public void run(String... args) {
		if (userRepository.existsByUsername(ADMIN_USERNAME)) return;
		userRepository.save(User.builder()
				.username(ADMIN_USERNAME)
				.fullName(ADMIN_FULLNAME)
				.email(ADMIN_USERNAME)
				.password(passwordEncoder.encode(ADMIN_PASSWORD))
				.role(UserRole.ADMIN)
				.enabled(true)
				.build()
		);

		log.info("Admin user '{}' created!", ADMIN_USERNAME);
	}

}
