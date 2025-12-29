package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.RatingRequest;
import com.finpro.twogoods.entity.*;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.repository.MerchantReviewRepository;
import com.finpro.twogoods.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantReviewService {

	private final MerchantReviewRepository reviewRepo;
	private final TransactionRepository transactionRepo;

	@Transactional(rollbackFor = Exception.class)
	public void giveRating(Long transactionId, RatingRequest request) {

		User user = getCurrentUser();

		Transaction trx = transactionRepo.findById(transactionId)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getCustomer().getId().equals(user.getId())) {
			throw new ApiException("You cannot rate someone else's transaction");
		}

		if (!trx.getStatus().equals(OrderStatus.COMPLETED)) {
			throw new ApiException("You can only rate completed transactions");
		}

		if (reviewRepo.existsByTransaction(trx)) {
			throw new ApiException("You already rated this transaction");
		}

		MerchantReview review = MerchantReview.builder()
				.merchant(trx.getMerchant())
				.user(user)
				.transaction(trx)
				.rating(request.getRating())
				.comment(request.getComment())
				.build();

		reviewRepo.save(review);
	}

	private User getCurrentUser() {
		return (User) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();
	}
}
