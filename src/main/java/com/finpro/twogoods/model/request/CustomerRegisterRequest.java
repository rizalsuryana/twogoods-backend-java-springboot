package com.finpro.twogoods.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRegisterRequest {
	@NotBlank
	private String fullName;
	@NotBlank
	@Email
	private String email;
	@NotBlank
	private String password;
}
