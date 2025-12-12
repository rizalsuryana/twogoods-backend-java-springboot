package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.ProductRequest;
import com.finpro.twogoods.dto.response.ProductImageResponse;
import com.finpro.twogoods.dto.response.ProductResponse;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.ProductImage;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.enums.ProductCondition;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import com.finpro.twogoods.repository.ProductImageRepository;
import com.finpro.twogoods.repository.ProductRepository;
import com.finpro.twogoods.utils.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final ProductImageRepository productImageRepository;
	private final CloudinaryService cloudinaryService;

	// CREATE PRODUCT
	@Transactional(rollbackFor = Exception.class)
	public ProductResponse createProduct(ProductRequest request) {

		User user = getCurrentUser();

		if (user.getRole() != UserRole.MERCHANT) {
			throw new AccessDeniedException("Only MERCHANT can create products");
		}

		MerchantProfile merchant = merchantProfileRepository.findByUser(user)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant profile not found"));

		// BLOCK MERCHANT YANG BELUM VERIFIED
		if (Boolean.FALSE.equals(merchant.getIsVerified())) {
			throw new AccessDeniedException("Merchant is not verified. Cannot create product.");
		}

		Product product = Product.builder()
				.merchant(merchant)
				.name(request.getName())
				.description(request.getDescription())
				.price(request.getPrice())
				.categories(request.getCategories())
				.color(request.getColor())
				.isAvailable(true)
				.condition(request.getCondition())
				.build();

		return productRepository.save(product).toResponse();
	}


	// GET PRODUCT BY ID
	@Transactional(readOnly = true)
	public ProductResponse getProductById(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		return product.toResponse();
	}
	// GET PRODUCTS WITH FILTER (multicategories + multi-keyword search)
	@Transactional(readOnly = true)
	public Page<ProductResponse> getProducts(
			int page,
			int size,
			String search,
			List<Categories> categories,
			BigDecimal minPrice,
			BigDecimal maxPrice,
			ProductCondition condition,
			Boolean isAvailable,
			String sort
	) {

		// Normalisasi sort
		if (sort == null || "null".equalsIgnoreCase(sort) || sort.isBlank()) {
			sort = "newest";
		}

		Pageable pageable = switch (sort) {
			case "price_asc" -> PageRequest.of(page, size, Sort.by("price").ascending());
			case "price_desc" -> PageRequest.of(page, size, Sort.by("price").descending());
			default -> PageRequest.of(page, size, Sort.by("createdAt").descending());
		};

		Specification<Product> spec = Specification.allOf();

		//  MULTI-KEYWORD SEARCH (AND)
		if (search != null && !search.isBlank()) {
			String[] keywords = search.trim().toLowerCase().split("\\s+");

			spec = spec.and((root, query, cb) -> {
				var namePath = cb.lower(cb.coalesce(root.get("name"), ""));
				List<Predicate> predicates = new ArrayList<>();

				for (String keyword : keywords) {
					predicates.add(cb.like(namePath, "%" + keyword + "%"));
				}

				return cb.and(predicates.toArray(Predicate[]::new));
			});
		}

//  MULTI-CATEGORY FILTER (AND): produk harus punya SEMUA kategori
		if (categories != null && !categories.isEmpty()) {
			spec = spec.and((root, query, cb) -> {
				query.distinct(true);

				var join = root.join("categories");

				// hitung berapa kategori yang match
				Predicate inCategories = join.in(categories);

				// group by product.id dan HAVING count(distinct category) = jumlah kategori yang diminta
				query.groupBy(root.get("id"));
				query.having(cb.equal(cb.countDistinct(join), categories.size()));

				return inCategories;
			});
		}


		//  PRICE FILTERS
		if (minPrice != null) {
			spec = spec.and((root, query, cb) ->
					cb.greaterThanOrEqualTo(root.get("price"), minPrice));
		}

		if (maxPrice != null) {
			spec = spec.and((root, query, cb) ->
					cb.lessThanOrEqualTo(root.get("price"), maxPrice));
		}

		//  CONDITION FILTER
		if (condition != null) {
			spec = spec.and((root, query, cb) ->
					cb.equal(root.get("condition"), condition));
		}

		//  AVAILABILITY FILTER
		if (isAvailable != null) {
			spec = spec.and((root, query, cb) ->
					cb.equal(root.get("isAvailable"), isAvailable));
		}

		Page<Product> result = productRepository.findAll(spec, pageable);

		return result.map(Product::toResponse);
	}


	// UPDATE PRODUCT
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
		if (request.getIsAvailable() != null) product.setIsAvailable(request.getIsAvailable());
		if (request.getCondition() != null) product.setCondition(request.getCondition());

		return productRepository.save(product).toResponse();
	}

	// DELETE PRODUCT
	@Transactional(rollbackFor = Exception.class)
	public void deleteProduct(Long id) {

		if (!isOwner(id)) {
			throw new AccessDeniedException("You can only delete your own product");
		}

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		productRepository.delete(product);
	}


	// UPLOAD MULTIPLE IMAGES
	@Transactional(rollbackFor = Exception.class)
	public List<ProductImageResponse> uploadMultipleImages(Long productId, MultipartFile[] files) throws IOException {

		if (!isOwner(productId)) {
			throw new AccessDeniedException("You can only upload images to your own product");
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		List<ProductImageResponse> responses = new ArrayList<>();

		for (MultipartFile file : files) {

			FileValidator.validateImage(file);

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

	// DELETE IMAGE
	@Transactional(rollbackFor = Exception.class)
	public void deleteProductImage(Long imageId) {

		ProductImage image = productImageRepository.findById(imageId)
				.orElseThrow(() -> new ResourceNotFoundException("Image not found"));

		productImageRepository.delete(image);
	}

	// OWNER CHECK
	@Transactional(readOnly = true)
	public boolean isOwner(Long productId) {

		User user = getCurrentUser();

		Product product = productRepository.findById(productId).orElse(null);
		if (product == null) return false;

		return product.getMerchant().getUser().getId().equals(user.getId());
	}

	private User getCurrentUser() {
		return (User) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();
	}
}
