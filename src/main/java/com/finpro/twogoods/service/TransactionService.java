package com.finpro.twogoods.service;

import com.finpro.twogoods.client.MidtransFeignClient;
import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import com.finpro.twogoods.dto.request.CreateTransactionRequest;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.entity.*;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final ProductRepository productRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final MidtransFeignClient midtransFeignClient;


	public MidtransSnapResponse createSnap(MidtransSnapRequest request) {
		System.out.println(">>> createSnap called");
		return midtransFeignClient.createTransaction(request);
	}



	// CREATE TRANSACTION
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse createTransaction(CreateTransactionRequest request) {

		User currentUser = getCurrentUser();

		if (!currentUser.getRole().equals(UserRole.CUSTOMER)) {
			throw new ApiException("Only customers can create transactions");
		}

		Product product = productRepository.findById(request.getProductId())
				.orElseThrow(() -> new ApiException("Product not found"));

		if (!product.getIsAvailable()) {
			throw new ApiException("Product is no longer available");
		}

		MerchantProfile merchant = product.getMerchant();

		if (merchant.getUser().getId().equals(currentUser.getId())) {
			throw new ApiException("Merchant cannot buy their own product");
		}

		// Create transaction
		Transaction transaction = Transaction.builder()
				.customer(currentUser)
				.merchant(merchant)
				.status(OrderStatus.PENDING)
				.totalPrice(BigDecimal.ZERO)
				.build();

		// Create item (quantity = 1)
		TransactionItem item = TransactionItem.builder()
				.transaction(transaction)
				.product(product)
				.price(product.getPrice())
				.quantity(1)
				.build();

		transaction.getItems().add(item);

		// Hitung total otomatis
		BigDecimal total = item.getSubtotal();
		transaction.setTotalPrice(total);

		// Simpan transaction + items (cascade)
		Transaction saved = transactionRepository.save(transaction);

		// Set product unavailable
		product.setIsAvailable(false);
		productRepository.save(product);

		return saved.toResponse();
	}

	// GET DETAIL TRANSACTION
	@Transactional(readOnly = true)
	public TransactionResponse getTransactionDetail(Long id) {

		User currentUser = getCurrentUser();

		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		boolean isCustomer = trx.getCustomer().getId().equals(currentUser.getId());
		boolean isMerchant = trx.getMerchant().getUser().getId().equals(currentUser.getId());

		if (!isCustomer && !isMerchant) {
			throw new ApiException("You are not allowed to view this transaction");
		}

		return trx.toResponse();
	}

	// GET CUSTOMER TRANSACTIONS
	@Transactional(readOnly = true)
	public List<TransactionResponse> getMyTransactions() {
		User customer = getCurrentUser();

		return transactionRepository.findByCustomer(customer)
				.stream()
				.map(Transaction::toResponse)
				.toList();
	}

	// GET MERCHANT ORDERS
	@Transactional(readOnly = true)
	public List<TransactionResponse> getMerchantOrders() {
		User merchantUser = getCurrentUser();

		MerchantProfile merchant = merchantProfileRepository.findByUser(merchantUser)
				.orElseThrow(() -> new ApiException("Merchant profile not found"));

		return transactionRepository.findByMerchant(merchant)
				.stream()
				.map(Transaction::toResponse)
				.toList();
	}

	// UPDATE STATUS
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse updateStatus(Long id, OrderStatus newStatus) {

		User currentUser = getCurrentUser();

		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		boolean isCustomer = trx.getCustomer().getId().equals(currentUser.getId());
		boolean isMerchant = trx.getMerchant().getUser().getId().equals(currentUser.getId());

		if (!isCustomer && !isMerchant) {
			throw new ApiException("You are not allowed to update this transaction");
		}

		OrderStatus currentStatus = trx.getStatus();

		switch (newStatus) {

			case PAID:
				if (!isMerchant) throw new ApiException("Only merchant can set PAID");
				if (currentStatus != OrderStatus.PENDING)
					throw new ApiException("PAID can only be set from PENDING");
				break;

			case SHIPPED:
				if (!isMerchant) throw new ApiException("Only merchant can set SHIPPED");
				if (currentStatus != OrderStatus.PAID)
					throw new ApiException("SHIPPED can only be set from PAID");
				break;

			case COMPLETED:
				if (!isCustomer) throw new ApiException("Only customer can set COMPLETED");
				if (currentStatus != OrderStatus.SHIPPED)
					throw new ApiException("COMPLETED can only be set from SHIPPED");
				break;

			case CANCELED:
				if (!isCustomer) throw new ApiException("Only customer can cancel");
				if (currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.COMPLETED) {
					throw new ApiException("Cannot cancel after item is shipped");
				}
				break;

			default:
				throw new ApiException("Invalid status update");
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
