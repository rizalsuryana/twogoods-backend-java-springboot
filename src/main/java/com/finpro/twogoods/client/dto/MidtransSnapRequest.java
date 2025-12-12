package com.finpro.twogoods.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidtransSnapRequest {

	@JsonProperty("transaction_details")
	private TransactionDetails transactionDetails;

	@JsonProperty("va_details")
	private List<VirtualAccount> vaDetails;

	private Callbacks callbacks;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
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


	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class VirtualAccount {
		private String bank;
		@JsonProperty("va_number")
		private String vaNumber;
		@JsonProperty("recipient_name")
		private String recipientName;
	}
}
