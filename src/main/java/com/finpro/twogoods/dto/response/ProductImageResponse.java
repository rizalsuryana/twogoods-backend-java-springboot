package com.finpro.twogoods.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {

	private Long id;

	private String imageUrl;
}
