package com.finpro.twogoods.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidtransSnapRequest {
	private TransactionDetails transaction_details;
	private CreditCard credit_card;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TransactionDetails {
		private String order_id;
		private Integer gross_amount;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CreditCard {
		private Boolean secure;
	}
}
