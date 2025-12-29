package com.finpro.twogoods.service;

import com.finpro.twogoods.client.MidtransFeignClient;
import com.finpro.twogoods.client.dto.MidtransRefundRequest;
import com.finpro.twogoods.client.dto.MidtransRefundResponse;
import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import com.finpro.twogoods.entity.Transaction;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static com.finpro.twogoods.enums.OrderStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MidtransService {

	private final MidtransFeignClient midtransFeignClient;
	private final TransactionRepository transactionRepository;

	@Value("${midtrans.api-key}")
	private String serverKey;

	@Transactional(rollbackFor = Exception.class)
	public MidtransSnapResponse createSnap(MidtransSnapRequest request) {
		log.error("CALL MIDTRANS SNAP ORDER_ID={}",
				  request.getTransactionDetails()
						 .getOrderId()
				 );

		return midtransFeignClient.createTransaction(request);
	}


	@Transactional(rollbackFor = Exception.class)
	public MidtransRefundResponse refund(String orderId, int amount) {

		Transaction trx = transactionRepository.findByOrderId(orderId)
											   .orElseThrow(() -> new ApiException("Transaction not found"));

		if (trx.getStatus() != OrderStatus.PAID &&
			trx.getStatus() != SHIPPED) {
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
		String raw = orderId + statusCode + grossAmount + serverKey;
		String hashed = DigestUtils.sha512Hex(raw);
		return hashed.equals(signatureKey);
	}

	public void updateStatus(Map<String, Object> payload) {

		String orderId = payload.get("order_id").toString();
		String transactionStatus = payload.get("transaction_status").toString();
		String fraudStatus = payload.getOrDefault("fraud_status", "").toString();

		Transaction trx = transactionRepository.findByOrderId(orderId)
											   .orElseThrow(() -> new ApiException("Transaction not found"));

		// Jangan ganggu fulfillment
		if (trx.getStatus() == PACKING
			|| trx.getStatus() == SHIPPED
			|| trx.getStatus() == DELIVERING
			|| trx.getStatus() == COMPLETED) {
			log.info("Skip Midtrans update for order {} status={}",
					 orderId, trx.getStatus()
					);
			return;
		}

		switch (transactionStatus) {

			case "capture" -> {
				if ("accept".equalsIgnoreCase(fraudStatus)) {
					markAsPaidIfNotYet(trx);
				}
			}

			case "settlement" -> markAsPaidIfNotYet(trx);

			case "pending" -> {
				// do nothing, already pending
			}

			case "expire", "cancel", "deny" -> {
				if (trx.getStatus() != PAID) {
					trx.setStatus(CANCELED);
				}
			}

			default -> log.info("Unhandled Midtrans status: {}", transactionStatus);
		}

		transactionRepository.save(trx);
	}


	private void markAsPaidIfNotYet(Transaction trx) {
		if (trx.getStatus() == PAID) return;

		trx.setStatus(PAID);
		trx.setPaidAt(LocalDateTime.now());
		trx.setAutoCancelAt(null);
	}


}
