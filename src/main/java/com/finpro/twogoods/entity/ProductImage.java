package com.finpro.twogoods.entity;

import com.finpro.twogoods.dto.response.ProductImageResponse;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage extends BaseEntity {


	private String imageUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	@JsonBackReference
	private Product product;

	public ProductImageResponse toResponse() {
		return ProductImageResponse.builder()
				.id(getId())
				.imageUrl(imageUrl)
				.build();
	}
}
