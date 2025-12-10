package com.finpro.twogoods.mapper;

import com.finpro.twogoods.dto.response.UserResponse;
import com.finpro.twogoods.entity.User;

public class UserMapper {

	public static UserResponse toSimple(User user) {
		return UserResponse.builder()
						   .id(user.getId())
						   .username(user.getUsername())
						   .email(user.getEmail())
						   .fullName(user.getFullName())
						   .role(user.getRole())
						   .profilePicture(user.getProfilePicture())
						   .build();
	}

	public static UserResponse toFull(User user) {
		return user.toResponse();
	}
}
