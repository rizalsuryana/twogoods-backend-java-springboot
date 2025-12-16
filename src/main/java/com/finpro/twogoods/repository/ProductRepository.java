package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.enums.ProductCondition;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
		JpaSpecificationExecutor<Product> {

	Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

	Page<Product> findByCategoriesContaining(Categories category, Pageable pageable);

	Page<Product> findByMerchant(MerchantProfile merchant, Pageable pageable);

	Page<Product> findByIsAvailableTrue(Pageable pageable);
	Product findFirstByMerchant(MerchantProfile merchant);

	@Query("""
    SELECT p FROM Product p
    WHERE :category MEMBER OF p.categories
    AND p.condition = :condition
""")
	List<Product> findSimilarProducts(
			@Param("category") Categories category,
			@Param("condition") ProductCondition condition
	);


//product si merhant
@Query("""
    SELECT p FROM Product p
    WHERE p.merchant.id = :merchantId
    AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
    AND (:available IS NULL OR p.isAvailable = :available)
    AND (:condition IS NULL OR p.condition = :condition)
    AND (:category IS NULL OR :category MEMBER OF p.categories)
""")
Page<Product> findMerchantProducts(
		@Param("merchantId") Long merchantId,
		@Param("search") String search,
		@Param("available") Boolean available,
		@Param("condition") ProductCondition condition,
		@Param("category") Categories category,
		Pageable pageable
);

}
