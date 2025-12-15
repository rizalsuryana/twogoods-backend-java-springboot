package com.finpro.twogoods.service;

import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.entity.*;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import com.finpro.twogoods.repository.ProductRepository;
import com.finpro.twogoods.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final ProductRepository productRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final MidtransService midtransService;


	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse buyNow(Long productId) {
		User user = getCurrentUser();

		if (!user.getRole().equals(UserRole.CUSTOMER)) {
			throw new ApiException("Only customers can buy products");
		}

		Product product = productRepository.findById(productId)
										   .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getIsAvailable()) {
			throw new ApiException("Product is sold out");
		}

		if (product.getMerchant().getUser().getId().equals(user.getId())) {
			throw new ApiException("Merchant cannot buy their own product");
		}

		Transaction trx = Transaction.builder()
									 .customer(user)
									 .merchant(product.getMerchant())
									 .status(OrderStatus.PENDING)
									 .totalPrice(product.getPrice())
									 .build();

		TransactionItem item = TransactionItem.builder()
											  .transaction(trx)
											  .product(product)
											  .price(product.getPrice())
											  .quantity(1)
											  .build();

		trx.getItems().add(item);

		String orderId = "ORDER-" + user.getId() + "-" + UUID.randomUUID();
		trx.setOrderId(orderId);

		Transaction saved = transactionRepository.save(trx);

		MidtransSnapRequest snapRequest =
				MidtransSnapRequest.builder()
								   .transactionDetails(
										   MidtransSnapRequest.TransactionDetails.builder()
																				 .orderId(orderId)
																				 .grossAmount(product.getPrice()
																									 .intValue())
																				 .build()
													  )
								   .callbacks(new MidtransSnapRequest.Callbacks(
										   "https://www.2goods.com"
								   ))
								   .build();

		MidtransSnapResponse snapResponse =
				midtransService.createSnap(snapRequest);


		TransactionResponse response = saved.toResponse();
		response.setMidtransSnapResponse(snapResponse);

		return response;
	}


	public TransactionResponse getTransactionDetail(Long id) {

		User currentUser = getCurrentUser();

		Transaction trx = transactionRepository.findById(id)
											   .orElseThrow(() -> new ResourceNotFoundException(
													   "Transaction not found"));

		boolean isCustomer =
				trx.getCustomer().getId().equals(currentUser.getId());

		boolean isMerchant =
				trx.getMerchant().getUser().getId().equals(currentUser.getId());

		if (!isCustomer && !isMerchant) {
			throw new ApiException("You are not allowed to view this transaction");
		}

		return trx.toResponse();
	}

	public List<TransactionResponse> getMyTransactions() {

		User customer = getCurrentUser();

		return transactionRepository.findByCustomer(customer)
									.stream()
									.map(Transaction::toResponse)
									.toList();
	}


	public List<TransactionResponse> getMerchantOrders() {
		User merchantUser = getCurrentUser();

		MerchantProfile merchant =
				merchantProfileRepository.findByUser(merchantUser)
										 .orElseThrow(() -> new ResourceNotFoundException(
												 "Merchant profile not found"
										 ));

		return transactionRepository.findByMerchant(merchant)
									.stream()
									.map(Transaction::toResponse)
									.toList();
	}


	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse updateStatus(Long id, OrderStatus newStatus) {

		User currentUser = getCurrentUser();

		Transaction trx = transactionRepository.findById(id)
											   .orElseThrow(() -> new ApiException("Transaction not found"));

		boolean isCustomer =
				trx.getCustomer().getId().equals(currentUser.getId());

		boolean isMerchant =
				trx.getMerchant().getUser().getId().equals(currentUser.getId());

		if (!isCustomer && !isMerchant) {
			throw new ApiException("You are not allowed to update this transaction");
		}

		OrderStatus currentStatus = trx.getStatus();

		switch (newStatus) {

			case PAID -> throw new ApiException(
					"PAID status is managed by Midtrans"
			);

			case SHIPPED -> {
				if (!isMerchant) {throw new ApiException("Only merchant can set SHIPPED");}
				if (currentStatus != OrderStatus.PAID) {throw new ApiException("SHIPPED only allowed after PAID");}
			}

			case COMPLETED -> {
				if (!isCustomer) {throw new ApiException("Only customer can complete order");}
				if (currentStatus != OrderStatus.SHIPPED) {
					throw new ApiException(
							"COMPLETED only allowed after SHIPPED"
					);
				}
			}

			case CANCELED -> {
				if (!isCustomer) {throw new ApiException("Only customer can cancel order");}
				if (currentStatus == OrderStatus.SHIPPED ||
					currentStatus == OrderStatus.COMPLETED) {
					throw new ApiException(
							"Cannot cancel after shipment"
					);
				}
			}

			default -> throw new ApiException("Invalid status update");
		}

		trx.setStatus(newStatus);
		Transaction updated = transactionRepository.save(trx);

		return updated.toResponse();
	}

	private User getCurrentUser() {
		return (User) SecurityContextHolder.getContext()
										   .getAuthentication()
										   .getPrincipal();
	}
}
