package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.CustomerRegisterRequest;
import com.finpro.twogoods.dto.request.MerchantRegisterRequest;
import com.finpro.twogoods.dto.request.UserRequest;
import com.finpro.twogoods.dto.response.UserResponse;
import com.finpro.twogoods.entity.CustomerProfile;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.exceptions.ResourceDuplicateException;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.helper.FilterHelper;
import com.finpro.twogoods.mapper.UserMapper;
import com.finpro.twogoods.repository.CustomerProfileRepository;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import com.finpro.twogoods.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
	private final CloudinaryService cloudinaryService;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Email or password is incorrect"));
	}

	@Transactional(rollbackFor = Exception.class)
	public User createCustomer(CustomerRegisterRequest request) {

		String username = validateUserOnRegister(
				request.getPassword(),
				request.getConfirmPassword(),
				request.getEmail()
		);

		User user = User.builder()
				.username(username)
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.fullName(request.getFullName())
				.role(UserRole.CUSTOMER)
				.enabled(true)
				.build();

		userRepository.save(user);

		customerProfileRepository.save(
				CustomerProfile.builder().user(user).build()
		);

		return user;
	}

	@Transactional(rollbackFor = Exception.class)
	public User createMerchant(MerchantRegisterRequest request) {

		String username = validateUserOnRegister(
				request.getPassword(),
				request.getConfirmPassword(),
				request.getEmail()
		);

		User user = User.builder()
				.username(username)
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.fullName(request.getFullName())
				.role(UserRole.MERCHANT)
				.enabled(true)
				.build();

		userRepository.save(user);

		merchantProfileRepository.save(
				MerchantProfile.builder()
						.user(user)
						.location(request.getLocation())
						.NIK(request.getNik())
						.rating(0)
						.build()
		);

		return user;
	}

	@Transactional(rollbackFor = Exception.class)
	public User updateUser(Long id, UserRequest request) {
		User user = getUserById(id);

		if (request.getEmail() != null && !request.getEmail().isBlank()) {
			if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
				throw new ResourceDuplicateException("Email already exists");
			}
			user.setEmail(request.getEmail());
		}

		if (request.getUsername() != null && !request.getUsername().isBlank()) {
			if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
				throw new ResourceDuplicateException("Username already exists");
			}
			user.setUsername(request.getUsername());
		}

		if (request.getFullName() != null && !request.getFullName().isBlank()) {
			user.setFullName(request.getFullName());
		}

		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
		}

		return userRepository.save(user);
	}

	@Transactional(rollbackFor = Exception.class)
	public User updateProfilePicture(Long userId, MultipartFile file) {
		User user = getUserById(userId);

		String imageUrl = cloudinaryService.uploadImage(file, "profile_pictures");

		user.setProfilePicture(imageUrl);

		return userRepository.save(user);
	}


	public Page<UserResponse> getAllUsers(int page, int size, String role, String search) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

		Page<User> usersPage = userRepository.findAll(pageable);

		List<User> filtered = usersPage.getContent();
		filtered = FilterHelper.filterByRole(filtered, role);
		filtered = FilterHelper.searchUsers(filtered, search);

		List<UserResponse> responses = filtered.stream()
				.map(User::toResponse)
				.toList();

		return new PageImpl<>(responses, pageable, usersPage.getTotalElements());
	}

	public UserResponse getMe() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User principal = (User) auth.getPrincipal();

		User user = userRepository.findById(principal.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		return UserMapper.toFull(user);
	}

	private String validateUserOnRegister(String password, String confirmPassword, String email) {

		if (!password.equals(confirmPassword)) {
			throw new IllegalArgumentException("Password and confirm password do not match");
		}

		if (userRepository.existsByEmail(email)) {
			throw new ResourceDuplicateException("Email already exists");
		}

		String username = email.split("@")[0];
		int counter = 1;
		String original = username;

		while (userRepository.existsByUsername(username)) {
			username = original + counter++;
		}

		return username;
	}

	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}
}
