package com.finpro.twogoods.dto.response;

import com.finpro.twogoods.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter @Builder
public class TransactionResponse {
	private Long id;
	private Long customerId;
	private Long merchantId;
	private OrderStatus status;
	private BigDecimal totalPrice;
	private String midtransOrderId;
	private String paymentUrl;
}

