package com.finpro.twogoods.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRegisterRequest {

	@Schema(example = "Rizal Saputra")
	@NotBlank(message = "Full name is required")
	@Size(max = 100, message = "Full name cannot exceed 100 characters")
	private String fullName;

	@Schema(example = "rizal@mail.com")
	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@Schema(example = "StrongPass!123")
	@NotBlank(message = "Password is required")
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
			message = "Password must be at least 8 chars, include uppercase, lowercase, number, and symbol"
	)
	private String password;

	@Schema(example = "StrongPass!123")
	@NotBlank(message = "Confirm password is required")
	private String confirmPassword;
}

