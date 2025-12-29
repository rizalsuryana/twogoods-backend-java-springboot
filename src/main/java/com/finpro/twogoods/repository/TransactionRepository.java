package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.Transaction;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findByCustomer(User customer);

	List<Transaction> findByMerchant(MerchantProfile merchant);

	Optional<Transaction> findByOrderId(String orderId);

	List<Transaction> findAllByOrderId(String orderId);

	List<Transaction> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime time);

	// auto cancel PAID
	List<Transaction> findByStatusAndAutoCancelAtBefore(OrderStatus status, LocalDateTime now);


	// PAGINATION + FILTER CUSTOMER
	@Query("""
    SELECT t FROM Transaction t
    JOIN t.items i
    WHERE t.customer.id = :customerId
    AND (:status IS NULL OR t.status = :status)
    AND (
        COALESCE(:search, '') = '' 
        OR LOWER(i.product.name) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%'))
    )
    AND (COALESCE(:startDate, t.createdAt) = t.createdAt OR t.createdAt >= :startDate)
    AND (COALESCE(:endDate, t.createdAt) = t.createdAt OR t.createdAt <= :endDate)
""")

	Page<Transaction> filterCustomerTransactions(
			@Param("customerId") Long customerId,
			@Param("status") OrderStatus status,
			@Param("search") String search,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate,
			Pageable pageable
	);


	// PAGINATION + FILTER MERCHANT
	@Query("""
    SELECT t FROM Transaction t
    JOIN t.items i
    WHERE t.merchant.id = :merchantId
    AND (:status IS NULL OR t.status = :status)
    AND (
        COALESCE(:search, '') = '' 
        OR LOWER(i.product.name) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%'))
    )
    AND (COALESCE(:startDate, t.createdAt) = t.createdAt OR t.createdAt >= :startDate)
    AND (COALESCE(:endDate, t.createdAt) = t.createdAt OR t.createdAt <= :endDate)
""")

	Page<Transaction> filterMerchantOrders(
			@Param("merchantId") Long merchantId,
			@Param("status") OrderStatus status,
			@Param("search") String search,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate,
			Pageable pageable
	);
}

//COALESCE --> ambil nilai pertama yang tidak null