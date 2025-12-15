package com.finpro.twogoods.controller;

import com.finpro.twogoods.client.dto.MidtransNotification;
import com.finpro.twogoods.client.dto.MidtransRefundResponse;
import com.finpro.twogoods.service.MidtransService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/midtrans")
@RequiredArgsConstructor
public class MidtransController {

	private final MidtransService midtransService;

	@PostMapping("/notification")
	public ResponseEntity<String> webhook(@RequestBody MidtransNotification notif) {

		boolean valid = midtransService.isValidSignature(
				notif.getOrderId(),
				notif.getStatusCode(),
				notif.getGrossAmount(),
				notif.getSignatureKey()
														);

		if (!valid) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID");
		}

		midtransService.updateStatus(notif);
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
