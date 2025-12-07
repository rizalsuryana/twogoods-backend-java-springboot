package com.finpro.twogoods.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
	@NotBlank
	private String password;

	@NotBlank
	private String email;

	@NotBlank
	private String fullName;

	@NotBlank
	private String profilePicture;
}
