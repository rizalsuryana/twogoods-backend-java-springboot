package com.finpro.twogoods.controller;

import com.finpro.twogoods.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/midtrans")
@RequiredArgsConstructor
public class MidtransWebhookController {

	private final TransactionService transactionService;

	@PostMapping("/callback")
	public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> payload,
												 @RequestHeader("X-Callback-Token") String callbackToken) {

		// Optional (recommended): Validate callback token
		if (!"MY_SECRET_TOKEN".equals(callbackToken)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid callback token");
		}

		transactionService.handleMidtransCallback(payload);

		return ResponseEntity.ok("OK");
	}
}
