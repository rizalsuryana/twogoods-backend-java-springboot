package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByEmailAndIdNot(String username, Long id);
	boolean existsByEmail(String email);
	List<User> findByEmail(String email);

    Optional<User> findUserByEmail(String email);
}
