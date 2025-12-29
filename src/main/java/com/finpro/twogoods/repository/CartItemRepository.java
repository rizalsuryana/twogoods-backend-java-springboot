package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.CartItem;
import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	List<CartItem> findByUser(User user);
	boolean existsByUserAndProduct(User user, Product product);
	List<CartItem> findByIdIn(List<Long> ids);
}
