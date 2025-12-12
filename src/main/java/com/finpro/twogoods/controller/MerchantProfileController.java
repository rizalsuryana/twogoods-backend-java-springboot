package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.request.MerchantProfileUpdateRequest;
import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.MerchantProfileResponse;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.service.MerchantProfileService;
import com.finpro.twogoods.service.UserService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchant-profiles")
@RequiredArgsConstructor
@Tag(name="Merchant-Profile")
public class MerchantProfileController {

	private final MerchantProfileService merchantProfileService;
	private final UserService userService;

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
	@PutMapping("/merchant/profile/{id}")
	public ResponseEntity<?> updateMerchantProfile(
			@PathVariable Long id,
			@Valid @RequestBody MerchantProfileUpdateRequest request
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Merchant profile updated",
				merchantProfileService.updateMerchantProfile(id, request)
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
