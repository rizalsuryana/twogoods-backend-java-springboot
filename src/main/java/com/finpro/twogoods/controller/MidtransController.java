package com.finpro.twogoods.controller;

import com.finpro.twogoods.client.dto.MidtransNotification;
import com.finpro.twogoods.client.dto.MidtransRefundResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.service.MidtransService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/midtrans")
@RequiredArgsConstructor
@Slf4j
public class MidtransController {

	private final MidtransService midtransService;

	@PostMapping("/notification")
	public ResponseEntity<String> webhook(
			@RequestBody Map<String, Object> payload
										 ) {
		try {
			log.info("MIDTRANS RAW NOTIF: {}", payload);

			String orderId = payload.get("order_id").toString();
			String statusCode = payload.get("status_code").toString();
			String grossAmount = payload.get("gross_amount").toString();
			String signatureKey = payload.get("signature_key").toString();

			boolean valid = midtransService.isValidSignature(
					orderId, statusCode, grossAmount, signatureKey
															);

			log.info("SIGNATURE VALID = {}", valid);

			if (valid) {
				midtransService.updateStatus(payload);
			} else {
				log.warn("INVALID SIGNATURE for orderId={}", orderId);
			}

		} catch (Exception e) {
			log.error("MIDTRANS WEBHOOK ERROR", e);
		}

		return ResponseEntity.ok("OK");
	}


	@PostMapping("/refund/{orderId}")
	public MidtransRefundResponse refund(
			@PathVariable String orderId,
			@RequestParam int amount
										) {
		return midtransService.refund(orderId, amount);
	}

	@PostMapping("/direct-refund/{orderId}")
	public MidtransRefundResponse directRefund(
			@PathVariable String orderId,
			@RequestParam int amount
											  ) {
		return midtransService.directRefund(orderId, amount);
	}
}
