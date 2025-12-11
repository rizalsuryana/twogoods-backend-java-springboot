package com.finpro.twogoods.service;

import com.finpro.twogoods.client.MidtransFeignClient;
import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MidtransService {

	private final MidtransFeignClient midtransFeignClient;

	public MidtransSnapResponse createSnap() {

		MidtransSnapRequest request = new MidtransSnapRequest(
						new MidtransSnapRequest.TransactionDetails(
										"order-id",
										10000
						),
						new MidtransSnapRequest.CreditCard(true)
		);

		// Your server key encoded in Base64
		String base64 = "Basic TWlkLXNlcnZlci1pZ2dhUktEMl85SEJMSTNFRUw0Qi1vbWQ6";

		return midtransFeignClient.createTransaction(base64, request);
	}

}
