// java
package com.finpro.twogoods.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.entity.*;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final UserRepository userRepository;
	private final TransactionItemRepository transactionItemRepository;
	private final ProductRepository productRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final ObjectMapper objectMapper;


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
