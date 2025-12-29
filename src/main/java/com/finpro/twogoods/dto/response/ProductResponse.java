package com.finpro.twogoods.dto.response;

import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.enums.ProductCondition;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

	private Long id;

	private Long merchantId;

	private String name;

	private String description;

	private BigDecimal price;

	private List<Categories> categories;

	private String color;

	private boolean isAvailable;

	private ProductCondition condition;

	private List<ProductImageResponse> images;
}
