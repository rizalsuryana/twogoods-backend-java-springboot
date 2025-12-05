package com.finpro.twogoods.entity;


import com.finpro.twogoods.model.response.UserResponse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchants")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Merchant extends BaseEntity {
	@Column(name = "merchant_name", nullable = false)
	private String merchantName;

	@Column(nullable = false)
	private String address;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id")
	private User user;


//	Helper untuk response
    public UserResponse toUserResponse() {
        return  UserResponse.builder()
                .email(user.getEmail())
                .role(user.getRole())
                .profilePicture(user.getProfilePicture())
                .location(user.getLocation())
                .name(merchantName)
                            .build();
    }
}
