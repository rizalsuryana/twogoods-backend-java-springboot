package com.finpro.twogoods.entity;

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

	@Column(nullable = false)
	private String location;
}
