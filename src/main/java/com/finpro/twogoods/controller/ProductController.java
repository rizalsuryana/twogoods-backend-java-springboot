package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.request.ProductRequest;
import com.finpro.twogoods.dto.request.SuggestPriceRequest;
import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.ProductImageResponse;
import com.finpro.twogoods.dto.response.ProductResponse;
import com.finpro.twogoods.dto.response.SuggestPriceResponse;
import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.enums.ProductCondition;
import com.finpro.twogoods.service.AiPriceService;
import com.finpro.twogoods.service.ProductService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management and catalog browsing")
public class ProductController {

	private final ProductService productService;
	private final AiPriceService aiPriceService;

	@Operation(summary = "Create product", description = "Create a new product. Only MERCHANT users can create products.")
	@PostMapping
	public ResponseEntity<ApiResponse<ProductResponse>> create(
			@RequestBody ProductRequest request
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.CREATED,
				"Product created successfully",
				productService.createProduct(request)
		);
	}

	@Operation(summary = "Get product by ID", description = "Fetch a single product by its ID.")
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> getById(
			@PathVariable Long id
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Product fetched successfully",
				productService.getProductById(id)
		);
	}

	@Operation(
			summary = "Get products with filters",
			description = """
                    Get paginated list of products with optional filters:
                    - search: multi-keyword search on product name (e.g. "vintage hoodie black")
                    - categories: filter by one or more categories (e.g. categories=Male,Shirt)
                    - minPrice / maxPrice: filter by price range
                    - condition: NEW or USED
                    - isAvailable: true or false
                    - sort: newest (default), price_asc, price_desc
                    """
	)
	@GetMapping
	public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
			@Parameter(
					description = "Page number (0-based). For first page use 0.",
					example = "0"
			)
			@RequestParam(defaultValue = "0") int page,

			@Parameter(
					description = "Number of items per page.",
					example = "10"
			)
			@RequestParam(defaultValue = "10") int size,

			@Parameter(
					description = "Search keyword(s) for product name. Supports multiple words (AND).",
					example = "vintage hoodie"
			)
			@RequestParam(required = false) String search,

			@Parameter(
					description = """
                            Filter by one or more categories.
                            Usage: ?categories=Male,Shirt
                            Backed by enum Categories.
                            """,
					array = @ArraySchema(schema = @Schema(implementation = Categories.class))
			)
			@RequestParam(required = false, name = "categories") List<Categories> categories,

			@Parameter(
					description = "Minimum product price filter.",
					example = "50000"
			)
			@RequestParam(required = false) BigDecimal minPrice,

			@Parameter(
					description = "Maximum product price filter.",
					example = "150000"
			)
			@RequestParam(required = false) BigDecimal maxPrice,

			@Parameter(
					description = "Product condition filter.",
					example = "USED"
			)
			@RequestParam(required = false) ProductCondition condition,

			@Parameter(
					description = "Availability filter. true = only available products.",
					example = "true"
			)
			@RequestParam(required = false) Boolean isAvailable,

			@Parameter(
					description = "Sorting options: newest (default), price_asc, price_desc",
					example = "price_asc"
			)
			@RequestParam(required = false) String sort
	) {
		Page<ProductResponse> result = productService.getProducts(
				page, size, search, categories, minPrice, maxPrice, condition, isAvailable, sort
		);

		return ResponseUtil.buildPagedResponse(
				HttpStatus.OK,
				"Products fetched successfully",
				result
		);
	}

	@Operation(summary = "Update product", description = "Update an existing product. Only the owner merchant can update.")
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductResponse>> update(
			@PathVariable Long id,
			@RequestBody ProductRequest request
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Product updated successfully",
				productService.updateProduct(id, request)
		);
	}

	@Operation(summary = "Delete product", description = "Delete a product. Only the owner merchant can delete.")
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		productService.deleteProduct(id);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.NO_CONTENT,
				"Product deleted successfully",
				null
		);
	}

	@Operation(summary = "Upload single product image", description = "Upload a single image for a product. Only the owner merchant can upload.")
	@PostMapping("/{productId}/upload-image")
	public ResponseEntity<ApiResponse<ProductImageResponse>> uploadImage(
			@PathVariable Long productId,
			@RequestParam("file") MultipartFile file
	) throws IOException {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Image uploaded successfully",
				productService.uploadProductImage(productId, file)
		);
	}

	@Operation(summary = "Upload multiple product images", description = "Upload multiple images for a product. Only the owner merchant can upload.")
	@PostMapping("/{productId}/upload-multi-images")
	public ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadMultiple(
			@PathVariable Long productId,
			@RequestParam("files") MultipartFile[] files
	) throws IOException {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Images uploaded successfully",
				productService.uploadMultipleImages(productId, files)
		);
	}

	@Operation(summary = "Delete product image", description = "Delete a single product image by its ID.")
	@DeleteMapping("/images/{imageId}")
	public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long imageId) {
		productService.deleteProductImage(imageId);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.NO_CONTENT,
				"Image deleted successfully",
				null
		);
	}

	@PostMapping("/suggest-price")
	public ResponseEntity<ApiResponse<SuggestPriceResponse>> suggestPrice(
			@RequestBody SuggestPriceRequest request
	) {
		SuggestPriceResponse response = aiPriceService.suggestPrice(request);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Price suggestion generated",
				response
		);
	}

}
