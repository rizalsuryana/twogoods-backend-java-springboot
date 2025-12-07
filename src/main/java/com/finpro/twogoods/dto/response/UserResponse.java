package com.finpro.twogoods.dto.response;

import com.finpro.twogoods.entity.CustomerProfile;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
	private Long id;
	private String username;
	private String email;
	private String fullName;
	private UserRole role;
	private String profilePicture;
	private MerchantProfile merchantProfile;
	private CustomerProfile customerProfile;
}
