package com.finpro.twogoods.entity;


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

	private String category;

	private String color;

	private boolean isAvailable;

	@Enumerated(EnumType.STRING)
	private ProductCondition condition;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductImage> images;

}
