package com.finpro.twogoods.controller;

import com.finpro.twogoods.entity.CustomerProfile;
import com.finpro.twogoods.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

	private final CustomerProfileRepository customerProfileRepository;

	@GetMapping("/{id}")
	public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
		CustomerProfile profile = customerProfileRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		return ResponseEntity.ok(profile);
	}
}
