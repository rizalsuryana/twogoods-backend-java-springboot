package com.finpro.twogoods.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.finpro.twogoods.dto.response.MerchantProfileResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

//	private float rating;

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	@JsonBackReference
	private User user;

	@Column(nullable = false)
	private String location;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
	private List<Product> products;

	public MerchantProfileResponse toResponse() {
		return MerchantProfileResponse.builder()
				.id(id)
				.location(location)
				.fullName(user != null ? user.getFullName() : null)
				.email(user != null ? user.getEmail() : null)
				.profilePicture(user != null ? user.getProfilePicture() : null)
				.role(user != null ? user.getRole() : null)
				.products(products == null
						? null
						: products.stream().map(Product::toResponse).toList())
				.build();
	}

}

//get all tambahin rating?