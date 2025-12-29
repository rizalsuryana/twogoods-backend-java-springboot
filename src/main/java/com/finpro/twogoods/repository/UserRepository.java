package com.finpro.twogoods.repository;

import com.finpro.twogoods.dto.request.SearchUserRequest;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.UserRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	static Specification<User> getSpecification(SearchUserRequest request) {
		return (root, criteriaQuery, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (request.getFullName() != null && !request.getFullName().isEmpty()) {
				predicates.add(criteriaBuilder.like(
						criteriaBuilder.lower(root.get("fullName")),
						"%" + request.getFullName().toLowerCase() + "%"
												   ));
				predicates.add(criteriaBuilder.like(
						criteriaBuilder.lower(root.get("email")),
						"%" + request.getFullName().toLowerCase() + "%"
												   ));
			}

			if (request.getRole() != null) {
				predicates.add(criteriaBuilder.equal(
						root.get("role"), request.getRole()
													));
			}

			return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
		};
	}

	Optional<User> findByEmail(String email);

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByUsernameAndIdNot(String username, Long id);

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByEmail(String email);

	Page<User> findAll(Specification<User> specification, Pageable pageable);

	@Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.merchantProfile mp
    LEFT JOIN FETCH u.customerProfile cp
    WHERE u.email = :email
""")
	Optional<User> findByEmailWithProfiles(String email);

}
