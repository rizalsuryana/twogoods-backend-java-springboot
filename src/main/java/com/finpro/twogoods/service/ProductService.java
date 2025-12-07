package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.ProductRequest;
import com.finpro.twogoods.dto.response.ProductImageResponse;
import com.finpro.twogoods.dto.response.ProductResponse;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.ProductImage;
import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import com.finpro.twogoods.repository.ProductImageRepository;
import com.finpro.twogoods.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final ProductImageRepository productImageRepository;
	private final CloudinaryService cloudinaryService;

	public ProductResponse createProduct(ProductRequest request) {
		MerchantProfile merchant = merchantProfileRepository.findById(request.getMerchantId())
				.orElseThrow(() -> new RuntimeException("Merchant not found"));

		Product product = Product.builder()
				.merchant(merchant)
				.name(request.getName())
				.description(request.getDescription())
				.price(request.getPrice())
				.categories(request.getCategories())
				.color(request.getColor())
				.isAvailable(request.isAvailable())
				.condition(request.getCondition())
				.build();

		Product saved = productRepository.save(product);
		return toProductResponse(saved);
	}

	public ProductResponse getProductById(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		return toProductResponse(product);
	}

	public Page<ProductResponse> getProducts(int page, int size, String search, Categories category) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

		Page<Product> result;

		if (search != null && !search.isBlank()) {
			result = productRepository.findByNameContainingIgnoreCase(search, pageable);
		} else if (category != null) {
			result = productRepository.findByCategoriesContaining(category, pageable);
		} else {
			result = productRepository.findAll(pageable);
		}

		return result.map(this::toProductResponse);
	}

	public ProductResponse updateProduct(Long id, ProductRequest request) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		if (request.getName() != null) product.setName(request.getName());
		if (request.getDescription() != null) product.setDescription(request.getDescription());
		if (request.getPrice() != null) product.setPrice(request.getPrice());
		if (request.getCategories() != null) product.setCategories(request.getCategories());
		if (request.getColor() != null) product.setColor(request.getColor());
		product.setAvailable(request.isAvailable());
		if (request.getCondition() != null) product.setCondition(request.getCondition());

		Product updated = productRepository.save(product);
		return toProductResponse(updated);
	}

	public void deleteProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		productRepository.delete(product);
	}

	public ProductImageResponse uploadProductImage(Long productId, MultipartFile file) throws IOException {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		String imageUrl = cloudinaryService.uploadImage(file);

		ProductImage image = ProductImage.builder()
				.product(product)
				.imageUrl(imageUrl)
				.build();

		ProductImage saved = productImageRepository.save(image);

		return ProductImageResponse.builder()
				.id(saved.getId())
				.imageUrl(saved.getImageUrl())
				.build();
	}

//	edit product sendiri
	public boolean isOwner(Long productId) {
		String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

		Product product = productRepository.findById(productId)
				.orElse(null);

		if (product == null) return false;

		Long merchantUserId = product.getMerchant().getUser().getId();

		return merchantUserId.toString().equals(currentUserId);
	}

//by Merchant
public Page<ProductResponse> getProductsByMerchant(Long merchantId, int page, int size) {
	MerchantProfile merchant = merchantProfileRepository.findById(merchantId)
			.orElseThrow(() -> new RuntimeException("Merchant not found"));

	Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

	Page<Product> result = productRepository.findByMerchant(merchant, pageable);

	return result.map(this::toProductResponse);
}

//Available product
public Page<ProductResponse> getAvailableProducts(int page, int size) {
	Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

	Page<Product> result = productRepository.findByIsAvailableTrue(pageable);

	return result.map(this::toProductResponse);
}


	private ProductResponse toProductResponse(Product product) {
		return ProductResponse.builder()
				.id(product.getId())
				.merchantId(product.getMerchant().getId())
				.name(product.getName())
				.description(product.getDescription())
				.price(product.getPrice())
				.categories(product.getCategories())
				.color(product.getColor())
				.isAvailable(product.isAvailable())
				.condition(product.getCondition())
				.images(product.getImages() == null ? null :
						product.getImages().stream()
								.map(img -> ProductImageResponse.builder()
										.id(img.getId())
										.imageUrl(img.getImageUrl())
										.build())
								.collect(Collectors.toList()))
				.build();
	}
}
