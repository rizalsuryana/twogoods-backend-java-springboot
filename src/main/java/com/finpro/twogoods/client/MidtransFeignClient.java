package com.finpro.twogoods.client;

import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import com.finpro.twogoods.config.MidtransFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
		name = "midtransClient",
		url = "https://app.sandbox.midtrans.com/snap/v1",
		configuration = MidtransFeignConfig.class
)
public interface MidtransFeignClient {

	@PostMapping(
			value = "/transactions",
			consumes = "application/json",
			produces = "application/json"
	)
	MidtransSnapResponse createTransaction(@RequestBody MidtransSnapRequest body);
}
