package com.finpro.twogoods.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Product extends BaseEntity {
	@Column(name = "product_name", nullable = false)
	private String productName;

	@Column(name = "product_pictures")
	private String productPicture;

// TODO	Relations
// TODO	toResponse (helper)
}
