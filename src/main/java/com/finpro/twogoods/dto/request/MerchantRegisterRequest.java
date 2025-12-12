package com.finpro.twogoods.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantRegisterRequest {

	@Schema(example = "Goods Store")
	@NotBlank(message = "Full name is required")
	private String fullName;

	@Schema(example = "goodstore@mail.com")
	@NotBlank
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


//	@NotBlank
//	private String location;
//	@NotBlank
//	private String nik; // nomor KTP
}
