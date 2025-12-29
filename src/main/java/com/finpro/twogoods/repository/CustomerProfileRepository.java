package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.CustomerProfile;
import com.finpro.twogoods.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
}
