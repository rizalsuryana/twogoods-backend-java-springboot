package com.finpro.twogoods.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MidtransSnapResponse {
	private String token;
	private String redirect_url;
}

