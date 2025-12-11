package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.response.CartItemResponse;
import com.finpro.twogoods.entity.CartItem;
import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.User;
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

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getIsAvailable()) {
			throw new ApiException("Product is sold out");
		}

		if (cartItemRepository.existsByUserAndProduct(user, product)) {
			throw new ApiException("Product already in cart");
		}

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
		CartItem item = cartItemRepository.findById(cartItemId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

		cartItemRepository.delete(item);
	}

//	hpus cmua
	public void clearCart() {
		User user = getCurrentUser();
		List<CartItem> items = cartItemRepository.findByUser(user);

		if (items.isEmpty()) {
			throw new ApiException("Cart is already empty");
		}

		cartItemRepository.deleteAll(items);
	}

//	hpus by toko
	public void clearCartByMerchant(Long merchantId) {
		User user = getCurrentUser();

		List<CartItem> items = cartItemRepository.findByUser(user)
				.stream()
				.filter(i -> i.getMerchant().getId().equals(merchantId))
				.toList();

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
