package com.finpro.twogoods.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MidtransSnapRequest {

	@JsonProperty("transaction_details")
	private TransactionDetails transactionDetails;

	private Callbacks callbacks;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class TransactionDetails {
		@JsonProperty("order_id")
		private String orderId;
		@JsonProperty("gross_amount")
		private Integer grossAmount;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Callbacks {
		private String finish;
	}
}
