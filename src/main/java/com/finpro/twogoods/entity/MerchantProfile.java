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
	// sama dengan user_id
	@Column(name = "user_id")
	private Long id;
	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	@JsonBackReference
	private User user;

	@Column(name = "nomor_ktp")
	private String NIK;

	@Column(name = "ktp_photo")
	private String ktpPhoto;

	@Column(name = "is_verified")
	private MerchantStatus isVerified = MerchantStatus.NEW;

	@Column(name = "reject_reason")
	private String rejectReason;

	private String location;



	@OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
	private List<Product> products;

	public MerchantProfileResponse toResponse() {
		return MerchantProfileResponse.builder()
				.id(id)
				.fullName(user != null ? user.getFullName() : null)
				.email(user != null ? user.getEmail() : null)
				.profilePicture(user != null ? user.getProfilePicture() : null)
				.role(user != null ? user.getRole() : null)
				.location(location)
				.nik(NIK)
				.ktpPhoto(ktpPhoto)
				.isVerified(isVerified)
				.rejectReason(rejectReason)
				.products(products == null ? null :
						products.stream().map(Product::toResponse).toList())
				.build();
	}
}
