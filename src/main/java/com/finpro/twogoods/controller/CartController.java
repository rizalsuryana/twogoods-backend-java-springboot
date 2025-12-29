package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.CartItemResponse;
import com.finpro.twogoods.service.CartService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart / Keranjang")
public class CartController {

	private final CartService cartService;

	@PostMapping("/{productId}")
	public ResponseEntity<ApiResponse<Object>> add(@PathVariable Long productId) {
		cartService.addToCart(productId);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Product added to cart",
				cartService.getMyCart()
		);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<CartItemResponse>>> getMyCart(){
		return ResponseUtil.buildListResponse(
				HttpStatus.OK,
				"Cart fetched successfully",
				cartService.getMyCart()
		);
	}

	@DeleteMapping("/{cartItemId}")
	public ResponseEntity<ApiResponse<Object>> remove(@PathVariable Long cartItemId){
		cartService.removeFromCart(cartItemId);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Item Removed from cart",
				cartService.getMyCart()
		);
	}

	@Operation()
	@DeleteMapping
	public ResponseEntity<ApiResponse<Object>> clearCart() {
		cartService.clearCart();
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"All items removed from cart",
				cartService.getMyCart()
		);
	}

	@DeleteMapping("/merchant/{merchantId}")
	public ResponseEntity<ApiResponse<Object>> clearCartByMerchant(@PathVariable Long merchantId) {
		cartService.clearCartByMerchant(merchantId);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Items from merchant removed from cart",
				cartService.getMyCart()
		);
	}

}
