package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.CustomerProfile;
import com.finpro.twogoods.entity.Transaction;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	List<Transaction> findByCustomer(User customer);
	List<Transaction> findByMerchant(MerchantProfile merchant);
}