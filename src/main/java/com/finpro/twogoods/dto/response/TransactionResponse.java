package com.finpro.twogoods.dto.response;

import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import com.finpro.twogoods.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
	private Long id;
	private String orderId;
	private Long customerId;
	private Long merchantId;
	private OrderStatus status;
	private BigDecimal totalPrice;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private MidtransSnapResponse midtransSnapResponse;

	private List<TransactionItemResponse> items;
}
