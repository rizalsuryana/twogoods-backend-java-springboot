package com.finpro.twogoods.repository;

import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail (String email);

	Optional<User> findByUsername (String username);

	boolean existsByUsername (String username);

	boolean existsByUsernameAndIdNot (String username, Long id);

	boolean existsByEmail (String email);

	List<User> findAllByRole (UserRole role);
}
