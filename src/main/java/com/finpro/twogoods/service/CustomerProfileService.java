package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.UserRequest;
import com.finpro.twogoods.entity.CustomerProfile;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomerProfileService {

	private final CustomerProfileRepository customerProfileRepository;
	private final UserService userService;

	public CustomerProfile getCustomerById(Long id) {
		return customerProfileRepository.findById(id)
										.orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
	}

	@Transactional(rollbackFor = Exception.class)
	public CustomerProfile updateCustomerProfile(Long id, CustomerProfile customerProfile) {

		CustomerProfile profile = getCustomerById(id);

		UserRequest userRequest = UserRequest.builder()
											 .profilePicture(customerProfile.getUser().getProfilePicture())
											 .email(customerProfile.getUser().getEmail())
											 .password(customerProfile.getUser().getPassword())
											 .fullName(customerProfile.getUser().getFullName())
											 .username(customerProfile.getUser().getUsername())
											 .build();

		userService.updateUser(customerProfile.getUser().getId(), userRequest);

		profile.setLocation(customerProfile.getLocation());

		return customerProfileRepository.save(profile);
	}

	public Page<CustomerProfile> getAllPaginated(Pageable pageable) {
		return customerProfileRepository.findAll(pageable);
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteCustomerProfileById(Long id) {
		CustomerProfile profile = customerProfileRepository.findById(id)
														   .orElseThrow(() -> new ResourceNotFoundException(
																   "Customer not found"));

		customerProfileRepository.delete(profile);
	}

	public List<CustomerProfile> getAllCustomerProfiles() {
		return customerProfileRepository.findAll();
	}
}
