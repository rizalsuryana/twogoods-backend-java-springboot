package com.finpro.twogoods.client;

import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
				name = "midtransClient",
				url = "https://app.sandbox.midtrans.com/snap/v1"
)
public interface MidtransFeignClient {

	@PostMapping(
					value = "/transactions",
					consumes = "application/json",
					produces = "application/json"
	)
	MidtransSnapResponse createTransaction(
					@RequestHeader("Authorization") String authorization,
					@RequestBody MidtransSnapRequest body
																				);
}

