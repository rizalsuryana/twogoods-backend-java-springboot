package com.finpro.twogoods.controller;

import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import com.finpro.twogoods.dto.request.CreateTransactionRequest;
import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.service.TransactionService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Customer transactions & merchant orders")
public class TransactionController {

	private final TransactionService transactionService;

// Buy Now
	@PostMapping("/buy-now/{productId}")
	public ResponseEntity<ApiResponse<TransactionResponse>> buyNow(
			@PathVariable Long productId
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.CREATED,
				"Buy now successful",
				transactionService.buyNow(productId)
		);
	}


	// GET DETAIL TRANSACTION
	@Operation(
			summary = "Get transaction detail",
			description = """
                    Get full detail of a transaction.
                    Only the customer or the merchant involved can view this.
                    """
	)
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<TransactionResponse>> getDetail(
			@PathVariable Long id
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Transaction detail fetched successfully",
				transactionService.getTransactionDetail(id)
		);
	}

	// GET CUSTOMER TRANSACTIONS
	@Operation(
			summary = "Get my transactions",
			description = "Get all transactions created by the logged-in customer."
	)
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<List<TransactionResponse>>> myTransactions() {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"My transactions fetched successfully",
				transactionService.getMyTransactions()
		);
	}

	// GET MERCHANT ORDERS
	@Operation(
			summary = "Get merchant orders",
			description = "Get all orders received by the logged-in merchant."
	)
	@GetMapping("/merchant")
	public ResponseEntity<ApiResponse<List<TransactionResponse>>> merchantOrders() {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Merchant orders fetched successfully",
				transactionService.getMerchantOrders()
		);
	}

	// UPDATE STATUS (Shopee Flow)
	@Operation(
			summary = "Update transaction status",
			description = """
                    Update transaction status following Shopee-like flow:
                    
                    CUSTOMER:
                    - COMPLETED (only after SHIPPED)
                    - CANCELED (only before SHIPPED)
                    
                    MERCHANT:
                    - PAID (only from PENDING)
                    - SHIPPED (only from PAID)
                    """
	)
	@PutMapping("/{id}/status")
	public ResponseEntity<ApiResponse<TransactionResponse>> updateStatus(
			@PathVariable Long id,
			@Parameter(
					description = "New status: PENDING, PAID, SHIPPED, COMPLETED, CANCELED",
					example = "PAID"
			)
			@RequestParam OrderStatus status
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Transaction status updated successfully",
				transactionService.updateStatus(id, status)
		);
	}
}
