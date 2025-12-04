package com.finpro.twogoods.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "merchants")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Merchant extends BaseEntity{
	@Column(name = "merchant_name", nullable = false)
	private String merchantName;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id")
	private User user;



//	Helper untuk response
}
