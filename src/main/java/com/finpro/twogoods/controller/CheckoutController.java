package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.request.CheckoutRequest;
import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.CheckoutResponse;
import com.finpro.twogoods.service.CheckoutService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout")
public class CheckoutController {

	private final CheckoutService checkoutService;

	@PostMapping
	public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
			@RequestBody CheckoutRequest request
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Checkout successful",
				checkoutService.checkout(request.getCartItemIds())
		);
	}
}
