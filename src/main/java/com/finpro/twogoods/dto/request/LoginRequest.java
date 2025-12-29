package com.finpro.twogoods.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
	@NotBlank
	private String email;
	@NotBlank
	private String password;
}
