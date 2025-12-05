package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.CustomerRegisterRequest;
import com.finpro.twogoods.dto.request.MerchantRegisterRequest;
import com.finpro.twogoods.dto.response.UserResponse;
import com.finpro.twogoods.entity.CustomerProfile;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.exceptions.ResourceDuplicateException;
import com.finpro.twogoods.repository.CustomerProfileRepository;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import com.finpro.twogoods.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final CustomerProfileRepository customerProfileRepository;
	private final MerchantProfileRepository merchantProfileRepository;


	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		log.debug("Loading user by email: {}", email);
		return userRepository.findByEmail(email).orElseThrow(() ->
				new UsernameNotFoundException("Email or password is incorrect"));
	}

	@Transactional
	public User createCustomer(CustomerRegisterRequest request) {

		if (!request.getPassword().equals(request.getConfirmPassword())) {
			throw new IllegalArgumentException("Password and confirm password do not match");
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ResourceDuplicateException("Email already exists");
		}

		// generate username dari email
		String username = request.getEmail().split("@")[0];
		int counter = 1;
		String originalUsername = username;

		while (userRepository.existsByUsername(username)) {
			username = originalUsername + counter++;
		}

		// buat user
		User user = User.builder()
				.username(username)
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.fullName(request.getFullName())
				.role(UserRole.CUSTOMER)
				.enabled(true)
				.build();

		userRepository.save(user);

		// buat customer profile
		CustomerProfile profile = CustomerProfile.builder()
				.user(user)
				.location(request.getLocation())
				.build();

		customerProfileRepository.save(profile);

		return user;
	}


	@Transactional
	public User createMerchant(MerchantRegisterRequest request) {

		if (!request.getPassword().equals(request.getConfirmPassword())) {
			throw new IllegalArgumentException("Password and confirm password do not match");
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ResourceDuplicateException("Email already exists");
		}

		// generate username dari email
		String username = request.getEmail().split("@")[0];
		int counter = 1;
		String originalUsername = username;

		while (userRepository.existsByUsername(username)) {
			username = originalUsername + counter++;
		}

		// buat user
		User user = User.builder()
				.username(username)
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.fullName(request.getFullName())
				.role(UserRole.MERCHANT)
				.enabled(true)
				.build();

		userRepository.save(user);

		// buat merchant profile
		MerchantProfile profile = MerchantProfile.builder()
				.user(user)
				.location(request.getLocation())
				.NIK(request.getNik())
				.rating(0)
				.build();

		merchantProfileRepository.save(profile);

		return user;
	}

	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream()
				.map(user -> UserResponse.builder()
						.id(user.getId())
						.username(user.getUsername())
						.email(user.getEmail())
						.fullName(user.getFullName())
						.role(user.getRole().getRoleName())
						.profilePicture(user.getProfilePicture())
						.build()
				).toList();
	}


}
