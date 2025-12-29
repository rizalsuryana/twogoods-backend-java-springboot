package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.response.CartItemResponse;
import com.finpro.twogoods.entity.CartItem;
import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.CartItemRepository;
import com.finpro.twogoods.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

	private final CartItemRepository cartItemRepository;
	private final ProductRepository productRepository;

	public void addToCart(Long productId) {
		User user = getCurrentUser();

		//  1. Block merchant from adding to cart
		if (user.getRole() == UserRole.MERCHANT) {
			throw new ApiException("Merchants cannot add items to cart");
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		//  2. Block buying own product
		if (product.getMerchant().getId().equals(user.getId())) {
			throw new ApiException("You cannot buy your own product");
		}

		//  3. Block unavailable product
		if (!product.getIsAvailable()) {
			throw new ApiException("Product is sold out");
		}

		//  4. Block duplicate cart item
		if (cartItemRepository.existsByUserAndProduct(user, product)) {
			throw new ApiException("Product already in cart");
		}

		//  5. Add to cart
		CartItem item = CartItem.builder()
				.user(user)
				.product(product)
				.merchant(product.getMerchant())
				.build();

		cartItemRepository.save(item);
	}

	public List<CartItemResponse> getMyCart() {
		User user = getCurrentUser();

		return cartItemRepository.findByUser(user)
				.stream()
				.map(CartItem::toResponse)
				.toList();
	}

	public void removeFromCart(Long cartItemId) {
		User user = getCurrentUser();

		CartItem item = cartItemRepository.findById(cartItemId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

		//  6. Block removing cart item that doesn't belong to user
		if (!item.getUser().getId().equals(user.getId())) {
			throw new ApiException("You cannot remove items from another user's cart");
		}

		cartItemRepository.delete(item);
	}

	public void clearCart() {
		User user = getCurrentUser();
		List<CartItem> items = cartItemRepository.findByUser(user);

		//  7. Block clearing empty cart
		if (items.isEmpty()) {
			throw new ApiException("Cart is already empty");
		}

		cartItemRepository.deleteAll(items);
	}

	public void clearCartByMerchant(Long merchantId) {
		User user = getCurrentUser();

		List<CartItem> items = cartItemRepository.findByUser(user)
				.stream()
				.filter(i -> i.getMerchant().getId().equals(merchantId))
				.toList();

		//  8. Block clearing merchant that doesn't exist in cart
		if (items.isEmpty()) {
			throw new ApiException("No items from this merchant in cart");
		}

		cartItemRepository.deleteAll(items);
	}

	private User getCurrentUser() {
		return (User) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();
	}
}

