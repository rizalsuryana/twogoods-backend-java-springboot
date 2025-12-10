package com.finpro.twogoods.client;

import com.finpro.twogoods.client.dto.MidtransChargeRequest;
import com.finpro.twogoods.dto.response.MidtransChargeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
		name = "midtransClient",
		url = "${midtrans.base-url}"
)
public interface MidtransFeignClient {

	@PostMapping("/charge")
	MidtransChargeResponse charge(@RequestBody MidtransChargeRequest request);

}
