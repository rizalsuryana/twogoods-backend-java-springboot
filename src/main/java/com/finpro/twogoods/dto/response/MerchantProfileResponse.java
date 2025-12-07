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
	private float rating;
	private String location;
	private String fullName;
	private String email;
	private String profilePicture;
	private UserRole role;
	private List<ProductResponse> products;
}
