package com.finpro.twogoods.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateTransactionRequest {
	private Long customerId;
	private Long merchantId;
	private Long productId;
}
