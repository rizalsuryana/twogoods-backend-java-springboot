package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.enums.ProductCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

	// PAGINATION PRODUCT MERCHANT
	@Query("""
			    SELECT DISTINCT p FROM Product p
			    WHERE p.merchant.id = :merchantId
			    AND (:available IS NULL OR p.isAvailable = :available)
			    AND (:condition IS NULL OR p.condition = :condition)
			    AND (
			        COALESCE(:search, '') = '' 
			        OR LOWER(p.name) LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%'))
			    )
			    AND (
			        :category IS NULL OR EXISTS (
			            SELECT 1 FROM p.categories c WHERE c = :category
			        )
			    )
			    ORDER BY p.createdAt DESC
			""")
	Page<Product> findMerchantProducts(
			@Param("merchantId") Long merchantId,
			@Param("search") String search,
			@Param("available") Boolean available,
			@Param("condition") ProductCondition condition,
			@Param("category") Categories category,
			Pageable pageable
	);


	//	random getall
	@Query(value = "SELECT * FROM products ORDER BY RANDOM()", nativeQuery = true) List<Product> findAllRandom();

}
