package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.CustomerRegisterRequest;
import com.finpro.twogoods.dto.request.LoginRequest;
import com.finpro.twogoods.dto.request.MerchantRegisterRequest;
import com.finpro.twogoods.dto.response.LoginResponse;
import com.finpro.twogoods.dto.response.RegisterResponse;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManager authenticationManager;
	private final UserService userService;

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword()
				)
		);

		User user = (User) authentication.getPrincipal();
		String token = jwtTokenProvider.generateToken(user);

		String location = null;
		if (user.getRole() == UserRole.CUSTOMER && user.getCustomerProfile() != null) {
			location = user.getCustomerProfile().getLocation();
		}
		if (user.getRole() == UserRole.MERCHANT && user.getMerchantProfile() != null) {
			location = user.getMerchantProfile().getLocation();
		}

		LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
				.userId(user.getId())
				.role(user.getRole().getRoleName())
				.email(user.getEmail())
				.name(user.getFullName())
				.profilePicture(user.getProfilePicture())
				.location(location)
				.build();

		return LoginResponse.builder()
				.accessToken(token)
				.tokenType("Bearer")
				.user(userInfo)
				.build();
	}

	@Transactional(rollbackFor = Exception.class)
	public RegisterResponse registerCustomer(CustomerRegisterRequest request) {
		log.info("Attempting to register CUSTOMER with email: {}", request.getEmail());

		User user = userService.createCustomer(request);

		log.info("Customer registered successfully with username: {}", user.getUsername());

		return RegisterResponse.builder()
				.fullName(user.getFullName())
				.email(user.getEmail())
				.build();
	}

	@Transactional(rollbackFor = Exception.class)
	public RegisterResponse registerMerchant(MerchantRegisterRequest request) {
		log.info("Attempting to register MERCHANT with email: {}", request.getEmail());

		User user = userService.createMerchant(request);

		log.info("Merchant registered successfully with username: {}", user.getUsername());

		return RegisterResponse.builder()
				.fullName(user.getFullName())
				.email(user.getEmail())
				.build();
	}
}
