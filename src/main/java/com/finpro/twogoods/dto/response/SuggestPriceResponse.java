package com.finpro.twogoods.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestPriceResponse {
	private BigDecimal recommendedPrice;
	private BigDecimal minRange;
	private BigDecimal maxRange;
	private String reasoning;
}
