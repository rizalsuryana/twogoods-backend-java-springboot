package com.finpro.twogoods.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.finpro.twogoods.dto.response.ProductImageResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String imageUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	@JsonBackReference
	private Product product;

	public ProductImageResponse toResponse() {
		return ProductImageResponse.builder()
								   .id(id)
								   .imageUrl(imageUrl)
								   .build();
	}
}
