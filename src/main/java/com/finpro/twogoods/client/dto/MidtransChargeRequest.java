package com.finpro.twogoods.client.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MidtransChargeRequest {
	private String payment_type;
	private TransactionDetails transaction_details;
	private Gopay gopay;
	private BankTransfer bank_transfer;


	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class TransactionDetails {
		private String order_id;
		private Integer gross_amount;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class Gopay {
		private Boolean enable_callback;
		private String callback_url;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class BankTransfer {
		private String bank;
	}


}