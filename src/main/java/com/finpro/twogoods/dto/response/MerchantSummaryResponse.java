package com.finpro.twogoods.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSummaryResponse {
	private Long id;
	private String fullName;
	private String email;
	private String profilePicture;
	private String location;
	private Float rating;
	private Long totalReviews;
}
