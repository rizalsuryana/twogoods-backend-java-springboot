package com.finpro.twogoods.entity;

import com.finpro.twogoods.dto.response.TransactionItemResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Transaction extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private User customer;

	@ManyToOne
	@JoinColumn(name = "merchant_id")
	private MerchantProfile merchant;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	private BigDecimal totalPrice;

	@OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL)
	@Builder.Default
	private List<TransactionItem> items = new ArrayList<>();



	public TransactionResponse toResponse() {
		return TransactionResponse.builder()
				.id(getId())
				.customerId(customer != null ? customer.getId() : null)
				.merchantId(merchant != null ? merchant.getId() : null)
				.status(status)
				.totalPrice(totalPrice)
				.createdAt(getCreatedAt())
				.updatedAt(getUpdatedAt())
				.items(
						items.stream()
								.map(item -> TransactionItemResponse.builder()
										.productId(item.getProduct().getId())
										.productName(item.getProduct().getName())
										.price(item.getPrice())
										.quantity(item.getQuantity())
										.build()
								).toList()
				)
				.build();
	}
}
