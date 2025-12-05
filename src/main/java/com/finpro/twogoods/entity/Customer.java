package com.finpro.twogoods.entity;

import com.finpro.twogoods.model.response.RegisterResponse;
import com.finpro.twogoods.model.response.UserResponse;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    public UserResponse toUserResponse() {
        return UserResponse.builder()
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole())
                .location(user.getLocation())
                .name(fullName)
                           .build();
    }

    public RegisterResponse toRegisterResponse() {
        return RegisterResponse.builder()
                .email(user.getEmail())
                .fullName(fullName)
                .email(user.getEmail())
                .userId(user.getId())
                               .build();
    }
}
