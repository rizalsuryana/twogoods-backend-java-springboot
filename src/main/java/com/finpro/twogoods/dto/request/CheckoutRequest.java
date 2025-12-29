package com.finpro.twogoods.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CheckoutRequest {
	private List<Long> cartItemIds;
}
