package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.request.ProductRequest;
import com.finpro.twogoods.dto.response.ProductImageResponse;
import com.finpro.twogoods.dto.response.ProductResponse;
import com.finpro.twogoods.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@PreAuthorize("hasRole('ADMIN') or (hasRole('MERCHANT') and @productService.isOwner(#id))")
	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
		return ResponseEntity.ok(productService.createProduct(request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
		return ResponseEntity.ok(productService.getProductById(id));
	}

	@GetMapping
	public ResponseEntity<Page<ProductResponse>> getProducts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String category
	) {
		return ResponseEntity.ok(productService.getProducts(page, size, search, category));
	}

	@PreAuthorize("hasRole('ADMIN') or (hasRole('MERCHANT') and @productService.isOwner(#id))")
	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> updateProduct(
			@PathVariable Long id,
			@RequestBody ProductRequest request
	) {
		return ResponseEntity.ok(productService.updateProduct(id, request));
	}

	@PreAuthorize("hasRole('ADMIN') or (hasRole('MERCHANT') and @productService.isOwner(#id))")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		productService.deleteProduct(id);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasRole('ADMIN') or (hasRole('MERCHANT') and @productService.isOwner(#id))")
	@PostMapping("/{productId}/upload-image")
	public ResponseEntity<ProductImageResponse> uploadProductImage(
			@PathVariable Long productId,
			@RequestParam("file") MultipartFile file
	) throws IOException {
		return ResponseEntity.ok(productService.uploadProductImage(productId, file));
	}

	@PreAuthorize("hasAnyRole('ADMIN','MERCHANT')")
	@GetMapping("/merchant/{merchantId}")
	public ResponseEntity<Page<ProductResponse>> getProductsByMerchant(
			@PathVariable Long merchantId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		return ResponseEntity.ok(productService.getProductsByMerchant(merchantId, page, size));
	}

	@GetMapping("/available")
	public ResponseEntity<Page<ProductResponse>> getAvailableProducts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		return ResponseEntity.ok(productService.getAvailableProducts(page, size));
	}

}
