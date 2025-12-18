package com.finpro.twogoods.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfileUpdateRequest {

	@Size(max = 200, message = "Location cannot exceed 200 characters")
	private String location;

	@Pattern(
			regexp = "^[0-9]{9,15}$",
			message = "Phone number must be 9–15 digits"
	)
	private String phoneNumber;
}
