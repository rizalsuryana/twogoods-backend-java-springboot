package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.service.MerchantSelfService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/merchant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@Tag(name = "Merchant KYC (Know Your Customer)")
public class MerchantSelfController {

	private final MerchantSelfService merchantSelfService;

	@PostMapping(
			value = "/upload-ktp",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	@Operation(summary = "Upload NIK", description = "Merchant upload its NIK for verif")
	public ResponseEntity<ApiResponse<Object>> uploadKtp(
			@RequestParam("file") MultipartFile file
	) {
		merchantSelfService.uploadKtp(file);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"KTP uploaded successfully",
				null
		);
	}
}
