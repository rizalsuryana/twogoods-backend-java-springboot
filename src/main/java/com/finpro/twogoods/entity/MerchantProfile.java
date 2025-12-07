package com.finpro.twogoods.entity;

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

	private float rating;

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	private String location;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
	private List<Product> products;

	public MerchantProfileResponse toResponse() {
		return MerchantProfileResponse.builder()
									  .id(id)
									  .profilePicture(getUser().getProfilePicture())
									  .role(getUser().getRole())
									  .email(getUser().getEmail())
									  .products(products == null
												? null
												: products.stream().map(Product::toResponse).toList())
									  .fullName(getUser().getFullName())
									  .location(getLocation())
									  .rating(getRating())
									  .build();
	}
}
