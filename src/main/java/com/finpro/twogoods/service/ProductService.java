package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.ProductRequest;
import com.finpro.twogoods.dto.response.ProductImageResponse;
import com.finpro.twogoods.dto.response.ProductResponse;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.ProductImage;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import com.finpro.twogoods.repository.ProductImageRepository;
import com.finpro.twogoods.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final ProductImageRepository productImageRepository;
	private final CloudinaryService cloudinaryService;

	//   CREATE PRODUCT — hanya merchant
	public ProductResponse createProduct(ProductRequest request) {

		User user = (User) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();

		if (!user.getRole().name().equals("MERCHANT")) {
			throw new AccessDeniedException("Only MERCHANT can create products");
		}

		MerchantProfile merchant = merchantProfileRepository.findByUser(user)
				.orElseThrow(() -> new RuntimeException("Merchant profile not found"));

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

	//   GET PRODUCT BY ID
	public ProductResponse getProductById(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		return toProductResponse(product);
	}

	//   GET ALL PRODUCTS (search + filter + paging)
	public Page<ProductResponse> getProducts(int page, int size, String search, Categories category) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

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

	//   UPDATE PRODUCT — hanya owner
	public ProductResponse updateProduct(Long id, ProductRequest request) {

		if (!isOwner(id)) {
			throw new AccessDeniedException("You can only update your own product");
		}

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

	//   DELETE PRODUCT — hanya owner
	public void deleteProduct(Long id) {

		if (!isOwner(id)) {
			throw new AccessDeniedException("You can only delete your own product");
		}

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		productRepository.delete(product);
	}

	//   UPLOAD IMAGE — hanya owner
	public ProductImageResponse uploadProductImage(Long productId, MultipartFile file) throws IOException {

		if (!isOwner(productId)) {
			throw new AccessDeniedException("You can only upload images to your own product");
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));
//		String imageUrl = cloudinaryService.uploadImage(file);
		String imageUrl = cloudinaryService.uploadImage(file, "products");
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

	//   CHECK OWNER
	public boolean isOwner(Long productId) {

		User user = (User) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();

		Product product = productRepository.findById(productId)
				.orElse(null);

		if (product == null) return false;

		Long merchantUserId = product.getMerchant().getUser().getId();

		return merchantUserId.equals(user.getId());
	}

	//   GET PRODUCTS BY MERCHANT
	public Page<ProductResponse> getProductsByMerchant(Long merchantId, int page, int size) {
		MerchantProfile merchant = merchantProfileRepository.findById(merchantId)
				.orElseThrow(() -> new RuntimeException("Merchant not found"));

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<Product> result = productRepository.findByMerchant(merchant, pageable);

		return result.map(this::toProductResponse);
	}

	//   GET AVAILABLE PRODUCTS
	public Page<ProductResponse> getAvailableProducts(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<Product> result = productRepository.findByIsAvailableTrue(pageable);

		return result.map(this::toProductResponse);
	}

//	Delete
public void deleteProductImage(Long imageId) {

	ProductImage image = productImageRepository.findById(imageId)
			.orElseThrow(() -> new RuntimeException("Image not found"));
//	bisa ditambah hapus dari si cloudinary jg nanti
	productImageRepository.delete(image);
}

	public boolean isOwnerByImage(Long imageId) {
	User user = (User) SecurityContextHolder.getContext()
			.getAuthentication()
			.getPrincipal();

	ProductImage image = productImageRepository.findById(imageId)
			.orElse(null);

	if (image == null) return false;

	Long merchantUserId = image.getProduct().getMerchant().getUser().getId();

	return merchantUserId.equals(user.getId());
}


	public List<ProductImageResponse> uploadMultipleImages(Long productId, MultipartFile[] files) throws IOException {

		if (!isOwner(productId)) {
			throw new AccessDeniedException("You can only upload images to your own product");
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		List<ProductImageResponse> responses = new ArrayList<>();

		for (MultipartFile file : files) {
//			String imageUrl = cloudinaryService.uploadImage(file);
			String imageUrl = cloudinaryService.uploadImage(file, "products");

			ProductImage image = ProductImage.builder()
					.product(product)
					.imageUrl(imageUrl)
					.build();

			ProductImage saved = productImageRepository.save(image);

			responses.add(
					ProductImageResponse.builder()
							.id(saved.getId())
							.imageUrl(saved.getImageUrl())
							.build()
			);
		}

		return responses;
	}





	//  MAPPER
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
