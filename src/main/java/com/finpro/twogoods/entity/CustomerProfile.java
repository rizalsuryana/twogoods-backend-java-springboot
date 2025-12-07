package com.finpro.twogoods.entity;

import com.finpro.twogoods.dto.response.CustomerProfileResponse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_profiles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfile {

	@Id
	private Long id; // sama dengan user_id

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	private User user;

	@Column
	private String location;

	public CustomerProfileResponse toResponse() {
		return CustomerProfileResponse.builder()
									  .role(getUser().getRole())
									  .customerId(getId())
									  .profilePicture(getUser().getProfilePicture())
									  .email(getUser().getEmail())
									  .fullName(getUser().getFullName())
									  .build();
	}
}
