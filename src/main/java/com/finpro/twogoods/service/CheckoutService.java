package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.entity.*;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.repository.CartItemRepository;
import com.finpro.twogoods.repository.ProductRepository;
import com.finpro.twogoods.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {
	private final CartItemRepository cartItemRepository;
	private final TransactionRepository transactionRepository;
	private final ProductRepository productRepository;

	@Transactional(rollbackFor = Exception.class)
	public List<TransactionResponse> checkout (){
		User user = getCurrentUser();

		List<CartItem> items = cartItemRepository.findByUser(user);

		if(items.isEmpty()){
			throw new ApiException("Cart is empty");
		}

		List<TransactionResponse> responses = new ArrayList<>();

		for (CartItem cart: items){
			Product product = cart.getProduct();

			if(!product.getIsAvailable()){
				throw new ApiException("Product " + product.getName() + "is sold out beybeh");
			}

			Transaction trx = Transaction.builder()
					.customer(user)
					.merchant(cart.getMerchant())
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

			Transaction saveTransaction = transactionRepository.save(trx);

			product.setIsAvailable(false);
			productRepository.save(product);

			responses.add(saveTransaction.toResponse());
		}
		cartItemRepository.deleteAll(items);
		return responses;
	}



	private User getCurrentUser(){
		return (User) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();
	}
}
