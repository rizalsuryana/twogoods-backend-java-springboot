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
@Table(
		name = "transactions",
		indexes = {
				@Index(name = "idx_transaction_order_id", columnList = "orderId")
		}
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Transaction extends BaseEntity {

	@Column(nullable = false, updatable = false)
	private String orderId;

	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private User customer;

	@ManyToOne
	@JoinColumn(name = "merchant_id", nullable = false)
	private MerchantProfile merchant;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Column(nullable = false)
	private BigDecimal totalPrice;

	@OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL)
	@Builder.Default
	private List<TransactionItem> items = new ArrayList<>();

	public TransactionResponse toResponse() {
		return TransactionResponse
				.builder()
				.id(getId())
				.orderId(orderId)
				.customerId(customer.getId())
				.merchantId(merchant.getId())
				.status(status)
				.totalPrice(totalPrice)
				.createdAt(getCreatedAt())
				.updatedAt(getUpdatedAt())
				.items(
						items.stream()
							 .map(item ->
										  TransactionItemResponse
												  .builder()
												  .productId(item.getProduct()
																 .getId())
												  .productName(item.getProduct()
																   .getName())
												  .price(item.getPrice())
												  .quantity(item.getQuantity())
												  .build()
								 ).toList()
					  )
				.build();
	}
}
