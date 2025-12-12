package com.finpro.twogoods.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingRequest {
	private Integer rating;
	private String comment;
}
