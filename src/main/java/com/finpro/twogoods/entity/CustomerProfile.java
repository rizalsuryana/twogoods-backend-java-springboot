package com.finpro.twogoods.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.finpro.twogoods.dto.response.CustomerProfileResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfile {

	@Id
	private Long id; // sama dengan user_id

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	@JsonBackReference
	private User user;

	@Column
	private String location;

	public CustomerProfileResponse toResponse() {
		return CustomerProfileResponse.builder()
									  .customerId(id)
									  .fullName(user != null ? user.getFullName() : null)
									  .email(user != null ? user.getEmail() : null)
									  .profilePicture(user != null ? user.getProfilePicture() : null)
									  .role(user != null ? user.getRole() : null)
									  .build();
	}
}
