package com.finpro.twogoods.dto.response;

import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {
	private List<TransactionResponse> transactions;
	private MidtransSnapResponse midtransSnap;
}
