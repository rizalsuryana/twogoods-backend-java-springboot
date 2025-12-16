package com.finpro.twogoods.controller;

import com.finpro.twogoods.client.dto.MidtransNotification;
import com.finpro.twogoods.client.dto.MidtransRefundResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.entity.Transaction;
import com.finpro.twogoods.service.MidtransService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/midtrans")
@RequiredArgsConstructor
@Slf4j
public class MidtransController {

	private final MidtransService midtransService;

	@PostMapping("/notification")
	public ResponseEntity<String> webhook(@RequestBody MidtransNotification notif) {

		log.info("MIDTRANS NOTIF RECEIVED: {}", notif);
		boolean valid = midtransService.isValidSignature(
				notif.getOrderId(),
				notif.getStatusCode(),
				notif.getGrossAmount(),
				notif.getSignatureKey()
														);

		log.info("SIGNATURE VALID = {}", valid);

		if (!valid) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID");
		}
		TransactionResponse trx = midtransService.updateStatus(notif);
		log.info("TRANSACTION UPDATED: {}", trx.getStatus());

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
