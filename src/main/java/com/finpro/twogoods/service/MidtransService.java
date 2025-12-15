package com.finpro.twogoods.service;

import com.finpro.twogoods.client.MidtransFeignClient;
import com.finpro.twogoods.client.dto.*;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.Transaction;
import com.finpro.twogoods.entity.TransactionItem;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.ProductRepository;
import com.finpro.twogoods.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MidtransService {

	private final MidtransFeignClient midtransFeignClient;
	private final TransactionRepository transactionRepository;

	@Value("${midtrans.api-key}")
	private String serverKey;

	@Transactional(rollbackFor = Exception.class)
	public MidtransSnapResponse createSnap(MidtransSnapRequest request) {
		return midtransFeignClient.createTransaction(request);
	}


	@Transactional(rollbackFor = Exception.class)
	public MidtransRefundResponse refund(String orderId, int amount) {

		Transaction trx = transactionRepository.findByOrderId(orderId)
											   .orElseThrow(() -> new ApiException("Transaction not found"));

		if (trx.getStatus() != OrderStatus.PAID &&
			trx.getStatus() != OrderStatus.SHIPPED) {
			throw new ApiException("Refund only allowed for PAID or SHIPPED transaction");
		}

		MidtransRefundRequest request = MidtransRefundRequest.builder()
															 .refund_amount(amount)
															 .refund_key("REF-" + System.currentTimeMillis())
															 .build();

		return midtransFeignClient.refund(orderId, request);
	}

	@Transactional(rollbackFor = Exception.class)
	public MidtransRefundResponse directRefund(String orderId, int amount) {

		Transaction trx = transactionRepository.findByOrderId(orderId)
											   .orElseThrow(() -> new ApiException("Transaction not found"));

		if (trx.getStatus() != OrderStatus.PAID) {
			throw new ApiException("Direct refund only allowed for PAID transaction");
		}

		MidtransRefundRequest request = MidtransRefundRequest.builder()
															 .refund_amount(amount)
															 .refund_key("DIRECT-" + System.currentTimeMillis())
															 .build();

		return midtransFeignClient.directRefund(orderId, request);
	}

	public boolean isValidSignature(
			String orderId,
			String statusCode,
			String grossAmount,
			String signatureKey
								   ) {
		try {
			String raw =
					orderId +
					statusCode +
					grossAmount +
					serverKey;

			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));

			StringBuilder hex = new StringBuilder();
			for (byte b : digest) {
				hex.append(String.format("%02x", b));
			}

			return hex.toString().equals(signatureKey);

		} catch (Exception e) {
			return false;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse updateStatus(MidtransNotification notif) {

		Transaction trx = transactionRepository.findByOrderId(notif.getOrderId())
											   .orElseThrow(() -> new ApiException("Order not found"));

		OrderStatus currentStatus = trx.getStatus();

		if (currentStatus == OrderStatus.PAID ||
			currentStatus == OrderStatus.SHIPPED ||
			currentStatus == OrderStatus.COMPLETED) {

			return trx.toResponse();
		}

		/* ================= STATUS MAPPING ================= */
		switch (notif.getTransactionStatus()) {

			case "capture":
				if ("accept".equalsIgnoreCase(notif.getFraudStatus())) {
					trx.setStatus(OrderStatus.PAID);
				}
				break;

			case "settlement":
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

			default:
				return trx.toResponse();
		}

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}
}
