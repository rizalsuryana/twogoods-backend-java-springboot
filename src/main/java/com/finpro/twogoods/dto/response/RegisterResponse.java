package com.finpro.twogoods.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResponse {
	private String fullName;
	private String email;
	private String phoneNumber;
}
