package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.enums.Categories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

	Page<Product> findByCategoriesContaining(Categories category, Pageable pageable);

	Page<Product> findByMerchant(MerchantProfile merchant, Pageable pageable);

	Page<Product> findByIsAvailableTrue(Pageable pageable);
}
