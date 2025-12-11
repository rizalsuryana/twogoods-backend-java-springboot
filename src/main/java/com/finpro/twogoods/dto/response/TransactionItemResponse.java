package com.finpro.twogoods.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionItemResponse {
	private Long productId;
	private String productName;
	private BigDecimal price;
	private Integer quantity;
}
