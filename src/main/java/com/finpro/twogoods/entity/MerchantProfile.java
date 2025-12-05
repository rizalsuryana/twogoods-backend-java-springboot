package com.finpro.twogoods.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "merchant_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantProfile {

	@Id
	private Long id; // sama dengan user_id

	@Column(name = "nomor_ktp")
	private String NIK;

	private int rating;

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	private String location;
}
