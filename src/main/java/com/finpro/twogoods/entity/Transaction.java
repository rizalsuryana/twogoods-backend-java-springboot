package com.finpro.twogoods.entity;

import com.finpro.twogoods.dto.response.TransactionItemResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

	//cancel both cust and merch....
	@Builder.Default
	private Boolean customerCancelRequest = false;

	@Builder.Default
	private Boolean merchantCancelConfirm = false;

	@Builder.Default
	private Boolean customerReturnRequest = false;

	@Builder.Default
	private Boolean merchantReturnConfirm = false;

	//	cancel return and cancel
	private LocalDateTime returnRequestedAt;
	private LocalDateTime cancelRequestedAt;

	//	auto cancel si paid
	private LocalDateTime paidAt;
	private LocalDateTime autoCancelAt;


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
				.orderId(getOrderId())
				.customerCancelRequest(customerCancelRequest)
				.merchantCancelConfirm(merchantCancelConfirm)
				.customerReturnRequest(customerReturnRequest)
				.merchantReturnConfirm(merchantReturnConfirm)
				.returnRequestedAt(returnRequestedAt)
				.customer(
						customer.getCustomerProfile() != null
								? customer.getCustomerProfile().toResponse()
								: null
				)
				.merchant(null)
				.items(
						items.stream()
								.map(item -> TransactionItemResponse.builder()
										.productId(item.getProduct().getId())
										.productName(item.getProduct().getName())
										.price(item.getPrice())
										.quantity(item.getQuantity())
										.productImage(
												item.getProduct().getImages() != null &&
														!item.getProduct().getImages().isEmpty()
														? item.getProduct().getImages().get(0).getImageUrl()
														: null
										)

										.build()
								).toList()
				)

				.build();
	}
}
