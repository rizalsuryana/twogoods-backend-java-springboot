package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.MerchantReview;
import com.finpro.twogoods.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MerchantReviewRepository extends JpaRepository<MerchantReview, Long> {

	boolean existsByTransaction(Transaction transaction);

	@Query("SELECT AVG(r.rating) FROM MerchantReview r WHERE r.merchant.id = :merchantId")
	Double getAverageRating(Long merchantId);

	@Query("SELECT COUNT(r) FROM MerchantReview r WHERE r.merchant.id = :merchantId")
	Long getTotalReviews(Long merchantId);
}
