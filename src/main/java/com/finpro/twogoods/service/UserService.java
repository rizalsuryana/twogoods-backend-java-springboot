package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.CustomerRegisterRequest;
import com.finpro.twogoods.dto.request.MerchantRegisterRequest;
import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.PagingResponse;
import com.finpro.twogoods.dto.response.StatusResponse;
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

	// REGISTER CUSTOMER
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

		User user = User.builder()
				.username(username)
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.fullName(request.getFullName())
				.role(UserRole.CUSTOMER)
				.enabled(true)
				.build();

		userRepository.save(user);

		CustomerProfile profile = CustomerProfile.builder()
				.user(user)
				.location(request.getLocation())
				.build();

		customerProfileRepository.save(profile);

		return user;
	}

	// REGISTER MERCHANT
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

		User user = User.builder()
				.username(username)
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.fullName(request.getFullName())
				.role(UserRole.MERCHANT)
				.enabled(true)
				.build();

		userRepository.save(user);

		MerchantProfile profile = MerchantProfile.builder()
				.user(user)
				.location(request.getLocation())
				.NIK(request.getNik())
				.rating(0)
				.build();

		merchantProfileRepository.save(profile);

		return user;
	}

	// GET ALL USERS WITH PAGINATION + FILTER + SEARCH
	public ApiResponse<List<UserResponse>> getAllUsers(int page, int size, String role, String search) {

		// Ambil semua user dulu (filter & search sebelum pagination)
		List<User> allUsers = userRepository.findAll();

		// FILTER BY ROLE
		if (role != null && !role.isEmpty()) {
			allUsers = allUsers.stream()
					.filter(u -> u.getRole().getRoleName().equalsIgnoreCase(role))
					.toList();
		}

		// SEARCH BY NAME OR EMAIL
		if (search != null && !search.isEmpty()) {
			String keyword = search.toLowerCase();
			allUsers = allUsers.stream()
					.filter(u ->
							(u.getFullName() != null && u.getFullName().toLowerCase().contains(keyword)) ||
									(u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword))
					)
					.toList();
		}

		int totalRows = allUsers.size();

		// PAGINATION MANUAL
		int start = page * size;
		int end = Math.min(start + size, totalRows);

		List<User> paginatedUsers = (start < end) ? allUsers.subList(start, end) : List.of();

		List<UserResponse> userResponses = paginatedUsers.stream()
				.map(user -> UserResponse.builder()
						.id(user.getId())
						.username(user.getUsername())
						.email(user.getEmail())
						.fullName(user.getFullName())
						.role(user.getRole().getRoleName())
						.profilePicture(user.getProfilePicture())
						.build()
				).toList();

		int totalPages = (int) Math.ceil((double) totalRows / size);

		PagingResponse paging = PagingResponse.builder()
				.page(page)
				.rowsPerPage(size)
				.totalRows((long) totalRows)
				.totalPages(totalPages)
				.hasNext(page + 1 < totalPages)
				.hasPrevious(page > 0)
				.build();

		return ApiResponse.<List<UserResponse>>builder()
				.status(new StatusResponse(200, "Users fetched successfully"))
				.data(userResponses)
				.paging(paging)
				.build();
	}
}
