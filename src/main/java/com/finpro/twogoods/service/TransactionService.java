// java
package com.finpro.twogoods.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finpro.twogoods.client.MidtransFeignClient;
import com.finpro.twogoods.client.dto.MidtransAction;
import com.finpro.twogoods.client.dto.MidtransChargeRequest;
import com.finpro.twogoods.dto.request.CreateTransactionRequest;
import com.finpro.twogoods.dto.response.MidtransChargeResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.entity.*;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final UserRepository userRepository;
	private final MidtransFeignClient midtransClient;
	private final TransactionItemRepository transactionItemRepository;
	private final ProductRepository productRepository;
	private final MerchantProfileRepository merchantProfileRepository;

	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse checkout(CreateTransactionRequest request) {

		User customer = getCustomer(request.getCustomerId());
		MerchantProfile merchant = getMerchant(request.getMerchantId());

		List<Product> products = loadProducts(request.getProductIds(), merchant);
		BigDecimal total = calculateTotal(products);
		String orderId = generateOrderId();

		Transaction trx = createTransaction(customer, merchant, orderId, total);
		saveTransactionItems(trx, products);

		MidtransChargeResponse midtransResponse = requestPaymentToMidtrans(orderId, total);
		String paymentUrl = extractPaymentUrl(midtransResponse);

		updateTransactionPaymentUrl(trx, paymentUrl);

		return trx.toResponse();
	}

	private User getCustomer(Long id) {
		return userRepository.findById(id)
							 .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
	}

	private MerchantProfile getMerchant(Long id) {
		return merchantProfileRepository.findById(id)
										.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));
	}

	private List<Product> loadProducts(List<Long> ids, MerchantProfile merchant) {
		List<Product> products = productRepository.findAllById(ids);

		if (products.isEmpty()) { throw new ApiException("No products found"); }

		boolean allMatch = products.stream()
								   .allMatch(p -> p.getMerchant().getId().equals(merchant.getId()));

		if (!allMatch) { throw new ApiException("All products must belong to the same merchant"); }

		// optional: validate count matches requested ids
		if (products.size() != ids.size()) {
			throw new ApiException("Some products not found");
		}

		return products;
	}

	private BigDecimal calculateTotal(List<Product> products) {
		return products.stream()
					   .map(Product::getPrice)
					   .reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private String generateOrderId() {
		return "ORDER-" + UUID.randomUUID();
	}

	private Transaction createTransaction(User customer, MerchantProfile merchant, String orderId, BigDecimal total) {
		Transaction trx = Transaction.builder()
									 .customer(customer)
									 .merchant(merchant)
									 .status(OrderStatus.PENDING)
									 .totalPrice(total)
									 .midtransOrderId(orderId)
									 .build();

		return transactionRepository.save(trx);
	}

	private void saveTransactionItems(Transaction trx, List<Product> products) {
		List<TransactionItem> items = new ArrayList<>(products.size());
		for (Product p : products) {
			items.add(TransactionItem.builder()
									 .transaction(trx)
									 .product(p)
									 .price(p.getPrice())
									 .build());
		}
		transactionItemRepository.saveAll(items);
	}

	private MidtransChargeResponse requestPaymentToMidtrans(String orderId, BigDecimal total) {

		MidtransChargeRequest request = MidtransChargeRequest.builder()
															 .payment_type("gopay")
															 .build();

		MidtransChargeRequest.Gopay gopay = new MidtransChargeRequest.Gopay();
		gopay.setEnable_callback(true);
		gopay.setCallback_url("https://yourdomain.com/payment/callback");
		request.setGopay(gopay);

		MidtransChargeRequest.TransactionDetails td = new MidtransChargeRequest.TransactionDetails();
		td.setOrder_id(orderId);
		// round to nearest whole unit (adjust if API expects cents)
		td.setGross_amount(total.setScale(0, RoundingMode.HALF_UP).intValue());
		request.setTransaction_details(td);

		return midtransClient.charge(request);
	}

	private String extractPaymentUrl(MidtransChargeResponse midResponse) {
		if (midResponse == null) { throw new ApiException("Midtrans: Empty response"); }

		List<MidtransAction> actions = midResponse.getActions();
		if (actions == null || actions.isEmpty()) { throw new ApiException("Midtrans: Missing actions[]"); }

		return actions.stream()
					  .filter(a -> "deeplink-redirect".equals(a.getName()))
					  .map(MidtransAction::getUrl)
					  .findFirst()
					  .orElseGet(() ->
										 actions.stream()
												.filter(a -> "generate-qr-code".equals(a.getName()))
												.map(MidtransAction::getUrl)
												.findFirst()
												.orElseThrow(() -> new ApiException("Midtrans: No payment URL available"))
								);
	}

	private void updateTransactionPaymentUrl(Transaction trx, String url) {
		trx.setPaymentUrl(url);
		transactionRepository.save(trx);
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

		MerchantProfile merchant = merchantProfileRepository.findByUser(merchantUser)
															.orElseThrow(() -> new ApiException("Merchant profile not found"));

		return transactionRepository.findByMerchant(merchant)
									.stream()
									.map(Transaction::toResponse)
									.toList();
	}

	public TransactionResponse updateStatus(Long id, OrderStatus newStatus) {

		User currentUser = getCurrentUser();

		Transaction trx = transactionRepository.findById(id)
											   .orElseThrow(() -> new ApiException("Transaction not found"));

		validateStatusUpdatePermission(trx, currentUser, newStatus);
		validateStatusFlow(trx.getStatus(), newStatus);

		trx.setStatus(newStatus);
		return transactionRepository.save(trx).toResponse();
	}

	private void validateStatusUpdatePermission(Transaction trx, User currentUser, OrderStatus newStatus) {

		boolean isCustomer = trx.getCustomer().getId().equals(currentUser.getId());
		boolean isMerchant = trx.getMerchant().getUser().getId().equals(currentUser.getId());

		if (!isCustomer && !isMerchant) { throw new ApiException("You are not allowed to update this transaction"); }

		if (newStatus == OrderStatus.PAID || newStatus == OrderStatus.SHIPPED) {
			if (!isMerchant) { throw new ApiException("Only merchant can update to " + newStatus); }
		}

		if (newStatus == OrderStatus.COMPLETED) {
			if (!isCustomer) { throw new ApiException("Only customer can set COMPLETED"); }
		}
	}

	private void validateStatusFlow(OrderStatus current, OrderStatus next) {
		switch (next) {
			case PAID -> {
				if (current != OrderStatus.PENDING) { throw new ApiException("PAID can only be set from PENDING"); }
			}
			case SHIPPED -> {
				if (current != OrderStatus.PAID) { throw new ApiException("SHIPPED can only be set from PAID"); }
			}
			case COMPLETED -> {
				if (current != OrderStatus.SHIPPED) { throw new ApiException("COMPLETED can only be set from SHIPPED"); }
			}
			case CANCELED -> {
				if (current == OrderStatus.SHIPPED || current == OrderStatus.COMPLETED) {
					throw new ApiException("Cannot cancel after item is shipped");
				}
			}
		}
	}

	private User getCurrentUser() {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		Object principal = auth.getPrincipal();
		if (principal instanceof User) {
			return (User) principal;
		}
		String username = auth.getName();
		return userRepository.findByUsername(username)
							 .orElseThrow(() -> new ApiException("User not found"));
	}
}
