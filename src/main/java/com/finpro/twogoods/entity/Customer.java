package com.finpro.twogoods.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Customer extends BaseEntity{

	@Column(name = "full_name", nullable = false)
	private  String fullName;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id")
	private User user;


//	helper
//=------------------==> student Response
}
