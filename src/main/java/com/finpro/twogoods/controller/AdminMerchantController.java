package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.MerchantProfileResponse;
import com.finpro.twogoods.service.AdminMerchantService;
import com.finpro.twogoods.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/merchants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMerchantController {

	private final AdminMerchantService adminMerchantService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<MerchantProfileResponse>>> getAll() {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"All merchants fetched",
				adminMerchantService.getAll()
		);
	}

	@GetMapping("/pending")
	public ResponseEntity<ApiResponse<List<MerchantProfileResponse>>> getPending() {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Pending merchants fetched",
				adminMerchantService.getPending()
		);
	}

	@GetMapping("/verified")
	public ResponseEntity<ApiResponse<List<MerchantProfileResponse>>> getVerified() {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Verified merchants fetched",
				adminMerchantService.getVerified()
		);
	}

	@PatchMapping("/{merchantId}/verify")
	public ResponseEntity<ApiResponse<Object>> verify(@PathVariable Long merchantId) {
		adminMerchantService.verify(merchantId);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Merchant verified successfully",
				null
		);
	}

	@PatchMapping("/{merchantId}/reject")
	public ResponseEntity<ApiResponse<Object>> reject(
			@PathVariable Long merchantId,
			@RequestParam String reason
	) {
		adminMerchantService.reject(merchantId, reason);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Merchant rejected",
				null
		);
	}

	@PatchMapping("/{merchantId}/disable")
	public ResponseEntity<ApiResponse<Object>> disable(@PathVariable Long merchantId) {
		adminMerchantService.disable(merchantId);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Merchant disabled",
				null
		);
	}

	@PatchMapping("/{merchantId}/enable")
	public ResponseEntity<ApiResponse<Object>> enable(@PathVariable Long merchantId) {
		adminMerchantService.enable(merchantId);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Merchant enabled",
				null
		);
	}

}
