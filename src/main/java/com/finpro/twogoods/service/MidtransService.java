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


		return midtransFeignClient.createTransaction( request);
	}

}
