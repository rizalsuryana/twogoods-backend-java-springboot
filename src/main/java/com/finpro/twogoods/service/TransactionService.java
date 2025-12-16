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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.finpro.twogoods.enums.OrderStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final ProductRepository productRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final MidtransService midtransService;

	// Buy now
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse buyNow(Long productId) {
		User user = getCurrentUser();

		String orderId = "ORDER-" + user.getId() + UUID.randomUUID();

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
				.orderId(orderId)
				.merchant(product.getMerchant())
				.status(PENDING)
				.totalPrice(product.getPrice())
				.build();

		TransactionItem item = TransactionItem.builder()
				.transaction(trx)
				.product(product)
				.price(product.getPrice())
				.quantity(1)
				.build();

		trx.getItems().add(item);

		Transaction saved = transactionRepository.save(trx);

		product.setIsAvailable(false);
		productRepository.save(product);

		TransactionResponse res = saved.toResponse();

		//Snap Request
		MidtransSnapRequest.TransactionDetails details =
				MidtransSnapRequest
						.TransactionDetails.builder()
										   .grossAmount(product.getPrice().intValue())
										   .orderId(orderId)
										   .build();

		MidtransSnapRequest req = MidtransSnapRequest.builder()
													 .transactionDetails(details)
													 .callbacks(new MidtransSnapRequest.Callbacks(
															 "https://www.2goods.com"))
													 .build();
		MidtransSnapResponse midtransResponse = midtransService.createSnap(req);
		log.info("CREATE SNAP WITH ORDER ID: {}", orderId);


		res.setMidtransSnapResponse(midtransResponse);

		return res;
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
															.orElseThrow(() -> new ApiException(
																	"Merchant profile not found"));

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
				throw new ApiException(
					"PAID status is managed by Midtrans"
			);
			case PACKING:
				if (!isMerchant) throw new ApiException("Only merchant can set PACKING");
				if (currentStatus != PAID) {
					throw new ApiException("PACKING can only be set from PAID");
				}
				break;

			case SHIPPED:
				if (!isMerchant) throw new ApiException("Only merchant can set SHIPPED");
				if (currentStatus != PACKING) {
					throw new ApiException("SHIPPED can only be set from PACKING");
				}
				break;

			case DELIVERING:
				if (!isMerchant) throw new ApiException("Only merchant can set DELIVERING");
				if (currentStatus != SHIPPED) {
					throw new ApiException("DELIVERING can only be set from SHIPPED");
				}
				break;

			case COMPLETED:
				if (!isCustomer) throw new ApiException("Only customer can set COMPLETED");
				if (currentStatus != DELIVERING) {
					throw new ApiException("COMPLETED can only be set from DELIVERING");
				}
				break;

			case CANCELED:
				if (currentStatus == SHIPPED
						|| currentStatus == DELIVERING
						|| currentStatus == COMPLETED) {
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

	//	Req cancel si cust
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse requestCancel(Long id) {
		User user = getCurrentUser();
		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getCustomer().getId().equals(user.getId())) {
			throw new ApiException("Only customer can request cancel");
		}

		if (trx.getStatus() == SHIPPED
				|| trx.getStatus() == DELIVERING
				|| trx.getStatus() == COMPLETED) {
			throw new ApiException("Cannot cancel after shipped");
		}

		trx.setCustomerCancelRequest(true);
//		counting wkwk
		trx.setCancelRequestedAt(LocalDateTime.now());

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}

	//merch confirm cancel
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse confirmCancel(Long id) {
		User user = getCurrentUser();
		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getMerchant().getUser().getId().equals(user.getId())) {
			throw new ApiException("Only merchant can confirm cancel");
		}

		if (!Boolean.TRUE.equals(trx.getCustomerCancelRequest())) {
			throw new ApiException("Customer has not requested cancel");
		}

		trx.setMerchantCancelConfirm(true);
		trx.setStatus(OrderStatus.CANCELED);

		if (trx.getStatus() == PENDING) {
			midtransService.directRefund(trx.getOrderId(), trx.getTotalPrice().intValue());
		}
		if (trx.getStatus() == PAID) {
			midtransService.refund(trx.getOrderId(), trx.getTotalPrice().intValue());
		}

		// balikin stock/availability product
		trx.getItems().forEach(item -> {
			Product p = item.getProduct();
			p.setIsAvailable(true);
			productRepository.save(p);
		});

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}


	//cust req return
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse requestReturn(Long id) {
		User user = getCurrentUser();
		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getCustomer().getId().equals(user.getId())) {
			throw new ApiException("Only customer can request return");
		}

		if (trx.getStatus() != OrderStatus.COMPLETED) {
			throw new ApiException("Return is only allowed after COMPLETED");
		}

		trx.setCustomerReturnRequest(true);
//		time for auto cancel
		trx.setReturnRequestedAt(LocalDateTime.now());

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}

	//merchant confirm return
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse confirmReturn(Long id) {
		User user = getCurrentUser();
		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getMerchant().getUser().getId().equals(user.getId())) {
			throw new ApiException("Only merchant can confirm return");
		}

		if (!Boolean.TRUE.equals(trx.getCustomerReturnRequest())) {
			throw new ApiException("Customer has not requested return");
		}

		if (trx.getStatus() == PENDING) {
			midtransService.directRefund(trx.getOrderId(), trx.getTotalPrice().intValue());
		}
		if (trx.getStatus() == PAID) {
			midtransService.refund(trx.getOrderId(), trx.getTotalPrice().intValue());
		}

		trx.setMerchantReturnConfirm(true);
		trx.setStatus(OrderStatus.RETURNED);

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}


	private User getCurrentUser() {
		return (User) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();
	}


//	Reject Cancel
@Transactional(rollbackFor = Exception.class)
public TransactionResponse rejectCancel(Long id) {
	User user = getCurrentUser();
	Transaction trx = transactionRepository.findById(id)
			.orElseThrow(() -> new ApiException("Transaction not found"));

	if (!trx.getMerchant().getUser().getId().equals(user.getId())) {
		throw new ApiException("Only merchant can reject cancel");
	}

	if (!Boolean.TRUE.equals(trx.getCustomerCancelRequest())) {
		throw new ApiException("No cancel request to reject");
	}

	trx.setCustomerCancelRequest(false);
	trx.setMerchantCancelConfirm(false);

	Transaction saved = transactionRepository.save(trx);
	return saved.toResponse();
}


//Reject Return
@Transactional(rollbackFor = Exception.class)
public TransactionResponse rejectReturn(Long id) {
	User user = getCurrentUser();
	Transaction trx = transactionRepository.findById(id)
			.orElseThrow(() -> new ApiException("Transaction not found"));

	if (!trx.getMerchant().getUser().getId().equals(user.getId())) {
		throw new ApiException("Only merchant can reject return");
	}

	if (!Boolean.TRUE.equals(trx.getCustomerReturnRequest())) {
		throw new ApiException("No return request to reject");
	}

	trx.setCustomerReturnRequest(false);
	trx.setMerchantReturnConfirm(false);

	Transaction saved = transactionRepository.save(trx);
	return saved.toResponse();
}

}
