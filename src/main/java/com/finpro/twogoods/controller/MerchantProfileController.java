package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.MerchantProfileResponse;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.service.MerchantProfileService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchant-profiles")
@RequiredArgsConstructor
@Tag(name="Merchant-Profile")
public class MerchantProfileController {

	private final MerchantProfileService merchantProfileService;

	//  GET PAGINATED
	@GetMapping
	public ResponseEntity<ApiResponse<List<MerchantProfileResponse>>> getAllPaginated(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		Page<MerchantProfileResponse> profiles =
				merchantProfileService.getAllPaginated(PageRequest.of(page, size));

		return ResponseUtil.buildPagedResponse(
				HttpStatus.OK,
				"Merchant profiles fetched successfully",
				profiles
		);
	}

	//  GET ALL
	@GetMapping("/all")
	public ResponseEntity<ApiResponse<List<MerchantProfileResponse>>> getAll() {
		List<MerchantProfileResponse> profiles =
				merchantProfileService.getAllMerchantProfiles();

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Merchant profiles fetched successfully",
				profiles
		);
	}

	//  GET BY ID
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<MerchantProfileResponse>> getById(@PathVariable Long id) {
		MerchantProfileResponse response = merchantProfileService.getMerchantById(id);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Merchant profile fetched successfully",
				response
		);
	}

	//  UPDATE
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<MerchantProfileResponse>> update(
			@PathVariable Long id,
			@RequestBody MerchantProfile merchantProfile,
			Authentication auth
	) {
		User user = (User) auth.getPrincipal();

		if (!user.getRole().name().equals("MERCHANT")) {
			throw new AccessDeniedException("Only MERCHANT can update merchant profile");
		}

		if (!user.getId().equals(id)) {
			throw new AccessDeniedException("You can only update your own merchant profile");
		}

		MerchantProfileResponse response =
				merchantProfileService.updateMerchantProfile(id, merchantProfile);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Merchant profile updated successfully",
				response
		);
	}

	//  DELETE
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication auth) {
		User user = (User) auth.getPrincipal();

		if (!user.getRole().name().equals("MERCHANT")) {
			throw new AccessDeniedException("Only MERCHANT can delete merchant profile");
		}

		if (!user.getId().equals(id)) {
			throw new AccessDeniedException("You can only delete your own merchant profile");
		}

		merchantProfileService.deleteMerchantProfileById(id);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.NO_CONTENT,
				"Merchant profile deleted successfully",
				null
		);
	}
}
