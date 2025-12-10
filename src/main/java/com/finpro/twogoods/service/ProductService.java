package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.ProductRequest;
import com.finpro.twogoods.dto.response.ProductImageResponse;
import com.finpro.twogoods.dto.response.ProductResponse;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.ProductImage;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import com.finpro.twogoods.repository.ProductImageRepository;
import com.finpro.twogoods.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final ProductImageRepository productImageRepository;
	private final CloudinaryService cloudinaryService;

	//  CREATE PRODUCT — hanya MERCHANT
	@Transactional(rollbackFor = Exception.class)
	public ProductResponse createProduct(ProductRequest request) {

		User user = getCurrentUser();

		if (user.getRole() != UserRole.MERCHANT) {
			throw new AccessDeniedException("Only MERCHANT can create products");
		}

		MerchantProfile merchant = merchantProfileRepository.findByUser(user)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant profile not found"));

		Product product = Product.builder()
				.merchant(merchant)
				.name(request.getName())
				.description(request.getDescription())
				.price(request.getPrice())
				.categories(request.getCategories())
				.color(request.getColor())
				.isAvailable(request.getIsAvailable() != null && request.getIsAvailable())
				.condition(request.getCondition())
				.build();

		return productRepository.save(product).toResponse();
	}

	//  GET PRODUCT BY ID
	@Transactional(readOnly = true)
	public ProductResponse getProductById(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		return product.toResponse();
	}

	//  GET ALL PRODUCTS (search + filter + paging)
	@Transactional(readOnly = true)
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

		return result.map(Product::toResponse);
	}

	//  UPDATE PRODUCT — hanya owner
	@Transactional(rollbackFor = Exception.class)
	public ProductResponse updateProduct(Long id, ProductRequest request) {

		if (!isOwner(id)) {
			throw new AccessDeniedException("You can only update your own product");
		}

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (request.getName() != null) product.setName(request.getName());
		if (request.getDescription() != null) product.setDescription(request.getDescription());
		if (request.getPrice() != null) product.setPrice(request.getPrice());
		if (request.getCategories() != null) product.setCategories(request.getCategories());
		if (request.getColor() != null) product.setColor(request.getColor());
		if (request.getIsAvailable() != null) product.setAvailable(request.getIsAvailable());
		if (request.getCondition() != null) product.setCondition(request.getCondition());

		return productRepository.save(product).toResponse();
	}

	//  DELETE PRODUCT — hanya owner
	@Transactional(rollbackFor = Exception.class)
	public void deleteProduct(Long id) {

		if (!isOwner(id)) {
			throw new AccessDeniedException("You can only delete your own product");
		}

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		productRepository.delete(product);
	}

	//  UPLOAD IMAGE — hanya owner
	@Transactional(rollbackFor = Exception.class)
	public ProductImageResponse uploadProductImage(Long productId, MultipartFile file) throws IOException {

		if (!isOwner(productId)) {
			throw new AccessDeniedException("You can only upload images to your own product");
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		String imageUrl = cloudinaryService.uploadImage(file, "products");

		ProductImage saved = productImageRepository.save(
				ProductImage.builder()
						.product(product)
						.imageUrl(imageUrl)
						.build()
		);

		return saved.toResponse();
	}

	//  GET PRODUCTS BY MERCHANT
	@Transactional(readOnly = true)
	public Page<ProductResponse> getProductsByMerchant(Long merchantId, int page, int size) {

		MerchantProfile merchant = merchantProfileRepository.findById(merchantId)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		return productRepository.findByMerchant(merchant, pageable)
				.map(Product::toResponse);
	}

	//  GET AVAILABLE PRODUCTS
	@Transactional(readOnly = true)
	public Page<ProductResponse> getAvailableProducts(int page, int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		return productRepository.findByIsAvailableTrue(pageable)
				.map(Product::toResponse);
	}

	//  DELETE PRODUCT IMAGE
	@Transactional(rollbackFor = Exception.class)
	public void deleteProductImage(Long imageId) {

		ProductImage image = productImageRepository.findById(imageId)
				.orElseThrow(() -> new ResourceNotFoundException("Image not found"));

		productImageRepository.delete(image);
	}

	//  CHECK OWNER BY IMAGE
	@Transactional(readOnly = true)
	public boolean isOwnerByImage(Long imageId) {

		User user = getCurrentUser();

		ProductImage image = productImageRepository.findById(imageId).orElse(null);
		if (image == null) return false;

		return image.getProduct().getMerchant().getUser().getId().equals(user.getId());
	}

	//  UPLOAD MULTIPLE IMAGES
	@Transactional(rollbackFor = Exception.class)
	public List<ProductImageResponse> uploadMultipleImages(Long productId, MultipartFile[] files) throws IOException {

		if (!isOwner(productId)) {
			throw new AccessDeniedException("You can only upload images to your own product");
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		List<ProductImageResponse> responses = new ArrayList<>();

		for (MultipartFile file : files) {

			String imageUrl = cloudinaryService.uploadImage(file, "products");

			ProductImage saved = productImageRepository.save(
					ProductImage.builder()
							.product(product)
							.imageUrl(imageUrl)
							.build()
			);

			responses.add(saved.toResponse());
		}

		return responses;
	}

	//  Helper: get current user
	private User getCurrentUser() {
		return (User) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();
	}

	//  Helper: check owner
	@Transactional(readOnly = true)
	public boolean isOwner(Long productId) {

		User user = getCurrentUser();

		Product product = productRepository.findById(productId).orElse(null);
		if (product == null) return false;

		return product.getMerchant().getUser().getId().equals(user.getId());
	}
}

