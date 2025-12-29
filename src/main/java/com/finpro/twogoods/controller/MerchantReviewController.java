package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.request.RatingRequest;
import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.service.MerchantReviewService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name="Review")
public class MerchantReviewController {

	private final MerchantReviewService reviewService;

	@PostMapping("/{transactionId}/rating")
	public ResponseEntity<ApiResponse<Object>> rate(
			@PathVariable Long transactionId,
			@RequestBody RatingRequest request
	) {
		reviewService.giveRating(transactionId, request);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Rating submitted",
				null
		);
	}
}
