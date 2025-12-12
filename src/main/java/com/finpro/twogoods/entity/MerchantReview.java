package com.finpro.twogoods.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "merchant_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantReview {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "merchant_id")
	private MerchantProfile merchant;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(optional = false)
	@JoinColumn(name = "transaction_id")
	private Transaction transaction;

	private Integer rating;
	private String comment;
}
