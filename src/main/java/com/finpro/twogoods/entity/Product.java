package com.finpro.twogoods.entity;


import com.finpro.twogoods.dto.response.ProductResponse;
import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.enums.ProductCondition;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id", nullable = false)
	private MerchantProfile merchant;

	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	private BigDecimal price;

	@ElementCollection(targetClass =Categories.class)
	@Enumerated(EnumType.STRING)
	@CollectionTable(
			name = "product_categories",
			joinColumns = @JoinColumn(name = "product_id")
	)
	@Column(name = "categories")
	private List<Categories> categories;

	private String color;

	private boolean isAvailable;

	@Enumerated(EnumType.STRING)
	private ProductCondition condition;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductImage> images;

	public ProductResponse toResponse() {
		return ProductResponse.builder()
							  .id(getId())
							  .categories(getCategories())
							  .merchantId(getMerchant().getId())
							  .color(getColor())
							  .condition(getCondition())
							  .description(getDescription())
							  .images(images.stream().map(ProductImage::toResponse).toList())
							  .isAvailable(isAvailable)
							  .price(getPrice())
							  .name(getName())
							  .build();
	}
}
