package com.finpro.twogoods.dto.request;

import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.enums.ProductCondition;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

	@NotBlank(message = "Product name is required")
	@Size(max = 100, message = "Product name cannot exceed 100 characters")
	private String name;

	@NotBlank(message = "Description is required")
	@Size(max = 1000, message = "Description cannot exceed 1000 characters")
	private String description;

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
	private BigDecimal price;

	@NotEmpty(message = "At least one category is required")
	private List<Categories> categories;

	@Size(max = 50, message = "Color cannot exceed 50 characters")
	private String color;

	private Boolean isAvailable;

	@NotNull(message = "Product condition is required")
	private ProductCondition condition;
}
