package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.request.CustomerProfileUpdateRequest;
import com.finpro.twogoods.dto.response.CustomerProfileResponse;
import com.finpro.twogoods.entity.CustomerProfile;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.service.CustomerProfileService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name="Customer-Profile")
public class CustomerController {

	private final CustomerProfileService customerProfileService;

	@GetMapping("/{id}")
	public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
		CustomerProfileResponse response = customerProfileService.getCustomerById(id).toResponse();
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				HttpStatus.OK.getReasonPhrase(),
				response);
	}

	@GetMapping
	public ResponseEntity<?> getAllCustomers(@RequestParam(defaultValue = "0") int page,
	                                         @RequestParam(defaultValue = "10") int size,
	                                         @RequestParam(defaultValue = "id,asc") String[] sort
	) {
		PageRequest pageRequest = PageRequest.of(page, size, getSort(sort));

		Page<CustomerProfileResponse> profiles =
				customerProfileService.getAllPaginated(pageRequest).map(CustomerProfile::toResponse);

		return ResponseUtil.buildPagedResponse(HttpStatus.OK, HttpStatus.OK.getReasonPhrase(), profiles);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateCustomer(
			@PathVariable Long id,
			@RequestBody CustomerProfileUpdateRequest request,
			Authentication auth
	) {
		User user = (User) auth.getPrincipal();

		// hanya CUSTOMER yang boleh update customer profile
		if (!user.getRole().name().equals("CUSTOMER")) {
			throw new AccessDeniedException("Only CUSTOMER can update customer profile");
		}

		// hanya boleh update profile miliknya sendiri
		if (!user.getId().equals(id)) {
			throw new AccessDeniedException("You can only update your own customer profile");
		}

		CustomerProfileResponse response =
				customerProfileService.updateCustomerProfile(id, request).toResponse();

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Customer profile updated successfully",
				response
		);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteCustomer(@PathVariable Long id, Authentication auth) {
		User user = (User) auth.getPrincipal();

		// hanya CUSTOMER yang boleh delete customer profile
		if (!user.getRole().name().equals("CUSTOMER")) {
			throw new AccessDeniedException("Only CUSTOMER can delete customer profile");
		}

		// hanya boleh delete profile miliknya sendiri
		if (!user.getId().equals(id)) {
			throw new AccessDeniedException("You can only delete your own customer profile");
		}

		customerProfileService.deleteCustomerProfileById(id);

		return ResponseUtil.buildSingleResponse(HttpStatus.OK, "Customer deleted successfully", null);
	}

	private Sort getSort(String[] sort) {
		String field = sort[0];
		String direction = sort.length > 1 ? sort[1] : "asc";

		return direction.equalsIgnoreCase("desc") ? Sort.by(field).descending() : Sort.by(field).ascending();
	}
}
