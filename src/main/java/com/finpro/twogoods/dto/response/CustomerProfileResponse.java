package com.finpro.twogoods.dto.response;

import com.finpro.twogoods.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfileResponse {
	private Long customerId;
	private String fullName;
	private String email;
	private String profilePicture;
	private UserRole role;
}
