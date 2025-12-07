package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.response.CustomerProfileResponse;
import com.finpro.twogoods.entity.CustomerProfile;
import com.finpro.twogoods.service.CustomerProfileService;
import com.finpro.twogoods.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping( "/api/v1/customers" )
@RequiredArgsConstructor
public class CustomerController {

	private final CustomerProfileService customerProfileService;

	@GetMapping( "/{id}" )
	public ResponseEntity<?> getCustomerById (@PathVariable Long id) {
		CustomerProfileResponse response = customerProfileService.getCustomerById(id).toResponse();
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				HttpStatus.OK.getReasonPhrase(),
				response);
	}

	@GetMapping
	public ResponseEntity<?> getAllCustomers (@RequestParam( defaultValue = "0" ) int page,
	                                          @RequestParam( defaultValue = "10" ) int size,
	                                          @RequestParam( defaultValue = "id,asc" ) String[] sort
	                                         ) {
		PageRequest pageRequest = PageRequest.of(page, size, getSort(sort));

		Page<CustomerProfileResponse> profiles =
				customerProfileService.getAllPaginated(pageRequest).map(CustomerProfile::toResponse);

		return ResponseUtil.buildPagedResponse(HttpStatus.OK, HttpStatus.OK.getReasonPhrase(), profiles);
	}

	@PutMapping( path = "/{id}" )
	public ResponseEntity<?> updateCustomer (@PathVariable Long id, @RequestBody CustomerProfile customerProfile) {
		CustomerProfileResponse response = customerProfileService.updateCustomerProfile(id, customerProfile).toResponse();
		return ResponseUtil.buildSingleResponse(
				HttpStatus.CREATED,
				HttpStatus.CREATED.getReasonPhrase(),
				response);
	}

	@DeleteMapping( "/{id}" )
	public ResponseEntity<?> deleteCustomer (@PathVariable Long id) {
		customerProfileService.deleteCustomerProfileById(id);
		return ResponseUtil.buildSingleResponse(HttpStatus.OK, "Customer deleted successfully", null);
	}

	private Sort getSort (String[] sort) {
		String field = sort[0];
		String direction = sort.length > 1 ? sort[1] : "asc";

		return direction.equalsIgnoreCase("desc") ? Sort.by(field).descending() : Sort.by(field).ascending();
	}
}
