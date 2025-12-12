package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantProfileRepository extends JpaRepository<MerchantProfile, Long> {

	Optional<MerchantProfile> findMerchantProfileById (Long id);
	Optional<MerchantProfile> findByUser(User user);

}
