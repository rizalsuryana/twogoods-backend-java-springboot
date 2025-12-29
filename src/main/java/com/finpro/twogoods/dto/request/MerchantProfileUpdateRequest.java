package com.finpro.twogoods.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantProfileUpdateRequest {
	@Pattern(regexp = "^[0-9]{16}$", message = "NIK must be 16 digits")
	private String nik;

	private String location;

}
