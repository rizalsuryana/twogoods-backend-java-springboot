package com.finpro.twogoods.client;

import com.finpro.twogoods.client.dto.MidtransRefundRequest;
import com.finpro.twogoods.client.dto.MidtransRefundResponse;
import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import com.finpro.twogoods.config.MidtransFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(
		name = "midtransClient",
		url = "https://app.sandbox.midtrans.com",
		configuration = MidtransFeignConfig.class
)
public interface MidtransFeignClient {

	@PostMapping("/snap/v1/transactions")
	MidtransSnapResponse createTransaction(@RequestBody MidtransSnapRequest body);

	@PostMapping("/v2/{orderId}/refund")
	MidtransRefundResponse refund(
			@PathVariable String orderId,
			@RequestBody MidtransRefundRequest body
								 );

	@PostMapping("/v2/{orderId}/direct_refund")
	MidtransRefundResponse directRefund(
			@PathVariable String orderId,
			@RequestBody MidtransRefundRequest body
									   );
}
