package com.finpro.twogoods.dto.request;

import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.enums.ProductCondition;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestPriceRequest {
	private String name;
	private String description;
	private List<Categories> categories;
	private ProductCondition condition;
}
