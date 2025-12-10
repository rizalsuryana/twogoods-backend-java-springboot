// java
package com.finpro.twogoods.dto.response;

import com.finpro.twogoods.client.dto.MidtransAction;
import lombok.Data;

import java.util.List;

@Data
public class MidtransChargeResponse {
	private String status_code;
	private String status_message;
	private String transaction_id;
	private String order_id;
	private String gross_amount;
	private List<MidtransAction> actions;
}
