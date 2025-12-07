package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.response.MerchantProfileResponse;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.service.MerchantProfileService;
import com.finpro.twogoods.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchant")
@RequiredArgsConstructor
public class MerchantProfileController {

	private final MerchantProfileService merchantProfileService;

	@GetMapping
	public ResponseEntity<?> getAllMerchants(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id,asc") String[] sort
											) {
		PageRequest pageRequest = PageRequest.of(page, size, getSort(sort));

		Page<MerchantProfileResponse> profiles =
				merchantProfileService.getMerchantProfiles(pageRequest).map(MerchantProfile::toResponse);

		return ResponseUtil.buildPagedResponse(HttpStatus.OK, HttpStatus.OK.getReasonPhrase(), profiles);
	}

	private Sort getSort(String[] sort) {
		String field = sort[0];
		String direction = sort.length > 1 ? sort[1] : "asc";

		return direction.equalsIgnoreCase("desc") ? Sort.by(field).descending() : Sort.by(field).ascending();
	}

	@GetMapping(path = "/{id}")
	public ResponseEntity<?> getMerchantProfileById(@PathVariable Long id) {
		MerchantProfileResponse response = merchantProfileService.getMerchantProfileById(id).toResponse();
		return ResponseUtil.buildSingleResponse(HttpStatus.OK, HttpStatus.OK.getReasonPhrase(), response);
	}

	@PutMapping(path = "/{id}")
	public ResponseEntity<?> updateMerchant (@PathVariable Long id, @RequestBody MerchantProfile merchant) {
		MerchantProfileResponse response = merchantProfileService.updateMerchantProfile(id, merchant).toResponse();
		return ResponseUtil.buildSingleResponse(HttpStatus.OK, HttpStatus.OK.getReasonPhrase(), response);
	}

	@DeleteMapping(path = "/{id}")
	public ResponseEntity<?> deleteMerchant (@PathVariable Long id) {
		merchantProfileService.deleteMerchantProfileById(id);
		return ResponseUtil.buildSingleResponse(HttpStatus.OK, HttpStatus.OK.getReasonPhrase(), null);
	}
}
