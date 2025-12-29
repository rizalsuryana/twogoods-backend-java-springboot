package com.finpro.twogoods.controller;


import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.PagedResult;
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
	public ResponseEntity<ApiResponse<PagedResult<TransactionResponse>>> getMyTransactions(
			@RequestParam(defaultValue = "0") Integer page,
			@RequestParam(name = "size", defaultValue = "10") Integer size,
			@RequestParam(required = false) OrderStatus status,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "DESC") String sortDir
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Customer transactions fetched successfully",
				transactionService.getMyTransactions(
						page, size, status, search, startDate, endDate, sortBy, sortDir
				)
		);
	}


	// GET MERCHANT ORDERS
	@Operation(
			summary = "Get merchant orders",
			description = "Get all orders received by the logged-in merchant."
	)
	@GetMapping("/merchant")
	public ResponseEntity<ApiResponse<PagedResult<TransactionResponse>>> merchantOrders(
			@RequestParam(defaultValue = "0") Integer page,
			@RequestParam(name = "size", defaultValue = "10") Integer size,
			@RequestParam(required = false) OrderStatus status,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "DESC") String sortDir
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Merchant orders fetched successfully",
				transactionService.getMerchantOrders(
						page, size, status, search, startDate, endDate, sortBy, sortDir
				)
		);
	}



	// UPDATE STATUS "neww
	@Operation(
			summary = "Update transaction status",
			description = """
                    Update transaction status flow:

				CUSTOMER:
				- COMPLETED (only after SHIPPED)
				- CANCELED (only before SHIPPED)

				MERCHANT:
				- SHIPPED (only after PAID)

				NOTE:
				- PAID status is controlled by Midtrans webhook
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


	// Customer request cancel
	@PostMapping("/{id}/request-cancel")
	public ResponseEntity<ApiResponse<TransactionResponse>> requestCancel(
			@PathVariable Long id
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Cancel request sent",
				transactionService.requestCancel(id)
		);
	}

	// Merchant confirm cancel
	@PostMapping("/{id}/confirm-cancel")
	public ResponseEntity<ApiResponse<TransactionResponse>> confirmCancel(
			@PathVariable Long id
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Cancel confirmed",
				transactionService.confirmCancel(id)
		);
	}

	// Customer request return
	@PostMapping("/{id}/request-return")
	public ResponseEntity<ApiResponse<TransactionResponse>> requestReturn(
			@PathVariable Long id
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Return request sent",
				transactionService.requestReturn(id)
		);
	}

	// Merchant confirm return
	@PostMapping("/{id}/confirm-return")
	public ResponseEntity<ApiResponse<TransactionResponse>> confirmReturn(
			@PathVariable Long id
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Return confirmed",
				transactionService.confirmReturn(id)
		);
	}

	// Merchant reject cancel
	@PostMapping("/{id}/reject-cancel")
	public ResponseEntity<ApiResponse<TransactionResponse>> rejectCancel(
			@PathVariable Long id
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Cancel request rejected",
				transactionService.rejectCancel(id)
		);
	}

	// Merchant reject return
	@PostMapping("/{id}/reject-return")
	public ResponseEntity<ApiResponse<TransactionResponse>> rejectReturn(
			@PathVariable Long id
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Return request rejected",
				transactionService.rejectReturn(id)
		);
	}

}
