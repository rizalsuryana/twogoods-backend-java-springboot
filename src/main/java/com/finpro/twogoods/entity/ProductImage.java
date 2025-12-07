package com.finpro.twogoods.entity;


import com.finpro.twogoods.dto.response.ProductImageResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	private String imageUrl;

	public ProductImageResponse toResponse() {
		return ProductImageResponse.builder()
								   .id(getId())
								   .imageUrl(imageUrl)
								   .build();
	}
}
