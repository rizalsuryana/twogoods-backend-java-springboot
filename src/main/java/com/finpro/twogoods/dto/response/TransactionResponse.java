package com.finpro.twogoods.dto.response;

import com.finpro.twogoods.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter @Builder
public class TransactionResponse {
	private Long id;
	private Long customerId;
	private Long merchantId;
	private OrderStatus status;
	private BigDecimal totalPrice;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}

