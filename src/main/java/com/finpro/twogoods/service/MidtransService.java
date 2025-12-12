package com.finpro.twogoods.service;

import com.finpro.twogoods.client.MidtransFeignClient;
import com.finpro.twogoods.client.dto.MidtransNotification;
import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.entity.Transaction;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.CustomerProfileRepository;
import com.finpro.twogoods.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MidtransService {

	private final MidtransFeignClient midtransFeignClient;
	private final TransactionRepository transactionRepository;
	private final CustomerProfileRepository customerProfileRepository;

	@Value("${midtrans.api-key}")
	private String serverKey;

	@Transactional(rollbackFor = Exception.class)
	public MidtransSnapResponse createSnap(MidtransSnapRequest request) {
		mapVirtualAccounts(request.getVaDetails());

		return midtransFeignClient.createTransaction(request);
	}

	private void mapVirtualAccounts(List<MidtransSnapRequest.VirtualAccount> vaDetails) {
		if (vaDetails == null) return;

		for (MidtransSnapRequest.VirtualAccount va : vaDetails) {
			String bankKey = va.getBank().toLowerCase() + "_va";
		}
	}


	public boolean isValidSignature(String orderId, String statusCode, String grossAmount, String signatureKey) {
		try {
			String raw = orderId + statusCode + grossAmount + serverKey;
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));

			StringBuilder hex = new StringBuilder();
			for (byte b : hash) hex.append(String.format("%02x", b));

			return hex.toString().equals(signatureKey);
		} catch (Exception e) {
			return false;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse updateStatus(MidtransNotification notif) {

		Transaction trx = transactionRepository.findByOrderId(notif.getOrderId())
											   .orElseThrow(() -> new RuntimeException("Order not found"));

		switch (notif.getTransactionStatus()) {
			case "settlement":
			case "capture":
				trx.setStatus(OrderStatus.PAID);
				break;
			case "pending":
				trx.setStatus(OrderStatus.PENDING);
				break;
			case "expire":
			case "cancel":
			case "deny":
				trx.setStatus(OrderStatus.CANCELED);
				break;
		}

		Transaction transaction = transactionRepository.save(trx);

		return transaction.toResponse();
	}
}
