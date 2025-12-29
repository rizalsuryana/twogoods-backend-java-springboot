package com.finpro.twogoods.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MidtransRefundResponse {
	private String status_code;
	private String status_message;
	private String transaction_id;
	private String order_id;
	private Integer refund_amount;
	private String refund_key;
}
