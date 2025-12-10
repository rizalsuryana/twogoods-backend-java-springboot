package com.finpro.twogoods.dto.response;

import lombok.*;

import java.math.BigDecimal;

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
	private Long merchantId;
}

