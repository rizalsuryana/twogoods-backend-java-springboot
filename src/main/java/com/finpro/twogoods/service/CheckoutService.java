package com.finpro.twogoods.service;

import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import com.finpro.twogoods.dto.response.CheckoutResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.entity.*;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.repository.CartItemRepository;
import com.finpro.twogoods.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckoutService {

	private final CartItemRepository cartItemRepository;
	private final TransactionRepository transactionRepository;
	private final MidtransService midtransService;

	@Transactional(rollbackFor = Exception.class)
	public CheckoutResponse checkout(List<Long> cartItemIds) {

		if (cartItemIds == null || cartItemIds.isEmpty()) {
			throw new ApiException("No cart items selected");
		}

		User user = getCurrentUser();
		List<CartItem> items = cartItemRepository.findByIdIn(cartItemIds);

		if (items.isEmpty()) {
			throw new ApiException("No valid cart items found for this user");
		}

		String orderId = "ORDER-" + user.getId() + "-" + UUID.randomUUID();
		int total = 0;

		List<TransactionResponse> responses = new ArrayList<>();

		for (CartItem cart : items) {

			Product product = cart.getProduct();

			if (!product.getIsAvailable()) {
				throw new ApiException("Product sold out");
			}

			Transaction trx = Transaction.builder()
										 .orderId(orderId)
										 .customer(user)
										 .merchant(cart.getMerchant())
										 .status(OrderStatus.PENDING)
										 .totalPrice(product.getPrice())
										 .build();

			trx.getItems().add(
					TransactionItem.builder()
								   .transaction(trx)
								   .product(product)
								   .price(product.getPrice())
								   .quantity(1)
								   .build()
							  );

			transactionRepository.save(trx);
			total += product.getPrice().intValue();

			responses.add(trx.toResponse());
		}

		// Hapus hanya cart item yang di-checkout
		cartItemRepository.deleteAll(items);


		MidtransSnapResponse snap = midtransService.createSnap(
				MidtransSnapRequest.builder()
								   .transactionDetails(
										   MidtransSnapRequest.TransactionDetails.builder()
																				 .orderId(orderId)
																				 .grossAmount(total)
																				 .build()
													  )
								   .callbacks(
										   new MidtransSnapRequest
												   .Callbacks("https://www.2goods.com"))
								   .build()
															  );

		responses.forEach((s) -> s.setMidtransSnapResponse(snap));

		return CheckoutResponse.builder()
							   .midtransSnap(snap)
							   .transactions(responses)
							   .build();
	}

	private User getCurrentUser() {
		return (User) SecurityContextHolder.getContext()
										   .getAuthentication().getPrincipal();
	}
}
