package com.finpro.twogoods.mapper;

import com.finpro.twogoods.dto.response.UserResponse;
import com.finpro.twogoods.entity.User;

public class UserMapper {
	public static UserResponse toFull(User user) {
		return user.toResponse();
	}
}
