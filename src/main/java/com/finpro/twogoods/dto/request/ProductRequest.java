package com.finpro.twogoods.dto.request;

import com.finpro.twogoods.enums.ProductCondition;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
	// merchant pemilik produk
	private Long merchantId;

	private String name;

	private String description;

	private BigDecimal price;

	private String category;

	private String color;

	private boolean isAvailable;

	private ProductCondition condition;
}
