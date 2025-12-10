package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.request.ProductRequest;
import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.ProductImageResponse;
import com.finpro.twogoods.dto.response.ProductResponse;
import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.service.ProductService;
import com.finpro.twogoods.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
	@PostMapping
	public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
			@RequestBody ProductRequest request
	) {
		ProductResponse response = productService.createProduct(request);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Product created successfully",
				response
		);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
		ProductResponse response = productService.getProductById(id);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Product fetched successfully",
				response
		);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Categories category
	) {
		Page<ProductResponse> products = productService.getProducts(page, size, search, category);

		return ResponseUtil.buildPagedResponse(
				HttpStatus.OK,
				"Products fetched successfully",
				products
		);
	}

	@PreAuthorize("hasRole('ADMIN') or (hasRole('MERCHANT') and @productService.isOwner(#id))")
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
			@PathVariable Long id,
			@RequestBody ProductRequest request
	) {
		ProductResponse response = productService.updateProduct(id, request);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Product updated successfully",
				response
		);
	}

	@PreAuthorize("hasRole('ADMIN') or (hasRole('MERCHANT') and @productService.isOwner(#id))")
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
		productService.deleteProduct(id);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.NO_CONTENT,
				"Product deleted successfully",
				null
		);
	}

	@PreAuthorize("hasRole('ADMIN') or (hasRole('MERCHANT') and @productService.isOwner(#productId))")
	@PostMapping("/{productId}/upload-image")
	public ResponseEntity<ApiResponse<ProductImageResponse>> uploadProductImage(
			@PathVariable Long productId,
			@RequestParam("file") MultipartFile file
	) throws IOException {

		ProductImageResponse response = productService.uploadProductImage(productId, file);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Product image uploaded successfully",
				response
		);
	}

	@PreAuthorize("hasAnyRole('ADMIN','MERCHANT')")
	@GetMapping("/merchant/{merchantId}")
	public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByMerchant(
			@PathVariable Long merchantId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		Page<ProductResponse> products = productService.getProductsByMerchant(merchantId, page, size);

		return ResponseUtil.buildPagedResponse(
				HttpStatus.OK,
				"Merchant products fetched successfully",
				products
		);
	}

	@GetMapping("/available")
	public ResponseEntity<ApiResponse<List<ProductResponse>>> getAvailableProducts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		Page<ProductResponse> products = productService.getAvailableProducts(page, size);

		return ResponseUtil.buildPagedResponse(
				HttpStatus.OK,
				"Available products fetched successfully",
				products
		);
	}

	@PreAuthorize("hasRole('ADMIN') or (hasRole('MERCHANT') and @productService.isOwnerByImage(#imageId))")
	@DeleteMapping("/images/{imageId}")
	public ResponseEntity<ApiResponse<Void>> deleteProductImage(@PathVariable Long imageId) {
		productService.deleteProductImage(imageId);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.NO_CONTENT,
				"Product image deleted successfully",
				null
		);
	}

	@PreAuthorize("hasRole('ADMIN') or (hasRole('MERCHANT') and @productService.isOwner(#productId))")
	@PostMapping("/{productId}/upload-multi-images")
	public ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadMultipleImages(
			@PathVariable Long productId,
			@RequestParam("files") MultipartFile[] files
	) throws IOException {

		List<ProductImageResponse> responses = productService.uploadMultipleImages(productId, files);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Product images uploaded successfully",
				responses
		);
	}
}

