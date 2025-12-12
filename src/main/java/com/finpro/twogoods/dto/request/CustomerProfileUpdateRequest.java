package com.finpro.twogoods.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfileUpdateRequest {
	private String location;
}
