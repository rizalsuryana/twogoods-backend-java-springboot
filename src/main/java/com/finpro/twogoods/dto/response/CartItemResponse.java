package com.finpro.twogoods.dto.response;

import com.finpro.twogoods.enums.Categories;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
	private Long id;

	private Long productId;
	private String productName;
	private BigDecimal price;
	private String productImage;
	private List<Categories> productCategories;

	private Long merchantId;
	private String merchantName;
	private String merchantEmail;
	private String merchantPhoto;
}

