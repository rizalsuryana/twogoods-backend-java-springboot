package com.finpro.twogoods.model.request;

import com.finpro.twogoods.entity.UserRole;
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
	private String   password;
	@NotBlank
	private UserRole role;
}