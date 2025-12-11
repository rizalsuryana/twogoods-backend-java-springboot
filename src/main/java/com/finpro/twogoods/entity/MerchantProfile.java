package com.finpro.twogoods.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.finpro.twogoods.dto.response.MerchantProfileResponse;
import com.finpro.twogoods.enums.MerchantStatus;
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
	@Column(name = "ktp_photo")
	private String ktpPhoto;

	@Column(name = "is_verified")
	private MerchantStatus isVerified = MerchantStatus.NEW;

	@Column(name = "reject_reason")
	private String rejectReason;

	private String location;

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	@JsonBackReference
	private User user;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
	private List<Product> products;


	public MerchantProfileResponse toResponse() {
		return MerchantProfileResponse.builder()
									  .id(id)
				.rating(rating)
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
