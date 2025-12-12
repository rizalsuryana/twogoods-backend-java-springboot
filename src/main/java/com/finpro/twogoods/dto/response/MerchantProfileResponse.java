package com.finpro.twogoods.dto.response;

import com.finpro.twogoods.enums.UserRole;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantProfileResponse {
	private Long id;
	private UserRole role;
	private String fullName;
	private String nik;
	private String location;
	private String email;
	private Float rating;
	private Long totalReviews;
	private String profilePicture;
	private String ktpPhoto;
	private Boolean isVerified;
	private String rejectReason;
	private List<ProductResponse> products;
}
