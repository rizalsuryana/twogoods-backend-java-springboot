package com.finpro.twogoods.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

	private String accessToken;
	private String refreshToken;
	private String tokenType;
	private Long tokenExpiration;

	private UserInfo user;

	@Getter @Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserInfo {
		private Long userId;
		private String role;
		private String email;
		private String name;
		private String profilePicture;
		private String location;
	}
}
