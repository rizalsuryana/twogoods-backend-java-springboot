package com.finpro.twogoods.entity;


import com.finpro.twogoods.dto.response.CartItemResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id", nullable = false)
	private MerchantProfile merchant;

	public CartItemResponse toResponse() {
		return CartItemResponse.builder()
				.id(getId())

				// Product
				.productId(product.getId())
				.productName(product.getName())
				.price(product.getPrice())
				.productImage(
						product.getImages() != null && !product.getImages().isEmpty()
								? product.getImages().get(0).getImageUrl()
								: null
				)
				.productCategories(product.getCategories())

				// Merchant
				.merchantId(merchant.getId())
				.merchantName(merchant.getUser().getFullName())
				.merchantEmail(merchant.getUser().getEmail())
				.merchantPhoto(merchant.getUser().getProfilePicture())

				.build();
	}

}
