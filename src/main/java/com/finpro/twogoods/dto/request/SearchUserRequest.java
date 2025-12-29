package com.finpro.twogoods.dto.request;

import com.finpro.twogoods.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchUserRequest {
	private String fullName;
	private UserRole role;
}
