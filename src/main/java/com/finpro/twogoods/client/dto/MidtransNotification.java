package com.finpro.twogoods.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MidtransNotification {
	@JsonProperty("transaction_status")
	private String transactionStatus;

	@JsonProperty("fraud_status")
	private String fraudStatus;

	@JsonProperty("order_id")
	private String orderId;

	@JsonProperty("status_code")
	private String statusCode;

	@JsonProperty("gross_amount")
	private String grossAmount;

	@JsonProperty("signature_key")
	private String signatureKey;
}

