package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.service.CheckoutService;
import com.finpro.twogoods.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {
	private final CheckoutService checkoutService;

	@PostMapping
	public ResponseEntity<ApiResponse<List<TransactionResponse>>> checkout(){
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Checkout successful",
				checkoutService.checkout()
		);
	}
}
