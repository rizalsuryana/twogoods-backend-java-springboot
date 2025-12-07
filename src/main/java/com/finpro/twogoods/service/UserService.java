package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.CustomerRegisterRequest;
import com.finpro.twogoods.dto.request.MerchantRegisterRequest;
import com.finpro.twogoods.dto.request.UserRequest;
import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.PagingResponse;
import com.finpro.twogoods.dto.response.StatusResponse;
import com.finpro.twogoods.dto.response.UserResponse;
import com.finpro.twogoods.entity.CustomerProfile;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.exceptions.ResourceDuplicateException;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
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
		log.debug("Loading user by email: {}", email);
		return userRepository.findByEmail(email)
							 .orElseThrow(() -> new UsernameNotFoundException("Email or password is incorrect"));
	}

	// REGISTER CUSTOMER
	@Transactional(rollbackFor = Exception.class)
	public User createCustomer(CustomerRegisterRequest request) {

		String username = validateUser(request.getPassword(), request.getConfirmPassword(), request.getEmail());

		User user = User.builder()
						.username(username)
						.email(request.getEmail())
						.password(passwordEncoder.encode(request.getPassword()))
						.fullName(request.getFullName())
						.role(UserRole.CUSTOMER)
						.enabled(true)
						.build();

		userRepository.save(user);

		CustomerProfile profile = CustomerProfile.builder().user(user).build();

		customerProfileRepository.save(profile);

		return user;
	}

	// REGISTER MERCHANT
	@Transactional(rollbackFor = Exception.class)
	public User createMerchant(MerchantRegisterRequest request) {

		String username = validateUser(request.getPassword(), request.getConfirmPassword(), request.getEmail());

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

	public User getUserById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not Found!"));
	}

	@Transactional(rollbackFor = Exception.class)
	public User updateUser(Long id, UserRequest request) {
		User user = getUserById(id);
		user.setFullName(request.getFullName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		return userRepository.save(user);
	}

	@Transactional
	public void updateExistingUser(User user, UserRequest request) {
		user.setFullName(request.getFullName());
		user.setEmail(request.getEmail());
		user.setFullName(request.getFullName());
		user.setProfilePicture(request.getProfilePicture());

		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
		}
	}

	public List<User> getAllUsersByRole(UserRole role) {
		return userRepository.findAllByRole(role);
	}


	private String validateUser(String request, String request1, String request2) {
		if (!request.equals(request1)) {
			throw new IllegalArgumentException("Password and confirm password do not match");
		}

		if (userRepository.existsByEmail(request2)) {
			throw new ResourceDuplicateException("Email already exists");
		}

		// generate username dari email
		String username = request2.split("@")[0];
		int counter = 1;
		String originalUsername = username;

		while (userRepository.existsByUsername(username)) {
			username = originalUsername + counter++;
		}
		return username;
	}

	@Transactional(rollbackFor = Exception.class)
	public User updateProfilePicture(Long userId, MultipartFile file) {
		User user = getUserById(userId);

		String imageUrl = cloudinaryService.uploadImage(file);
		user.setProfilePicture(imageUrl);

		return userRepository.save(user);
	}


	// GET ALL USERS WITH PAGINATION + FILTER + SEARCH
	public ApiResponse<List<UserResponse>> getAllUsers(int page, int size, String role, String search) {

		// Ambil semua user dulu (filter & search sebelum pagination)
		List<User> allUsers = userRepository.findAll();

		// FILTER BY ROLE
		if (role != null && !role.isEmpty()) {
			allUsers = allUsers.stream().filter(u -> u.getRole().getRoleName().equalsIgnoreCase(role)).toList();
		}

		// SEARCH BY NAME OR EMAIL
		if (search != null && !search.isEmpty()) {
			String keyword = search.toLowerCase();
			allUsers = allUsers.stream()
							   .filter(u -> (
													u.getFullName() != null && u.getFullName()
																				.toLowerCase()
																				.contains(keyword)) || (
													u.getEmail() != null && u.getEmail()
																			 .toLowerCase()
																			 .contains(
																					 keyword)))
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
																				  .role(user.getRole())
																				  .profilePicture(user.getProfilePicture())
																				  .build())
														 .toList();

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
