package com.finpro.twogoods.utils.helper;

import com.finpro.twogoods.entity.User;

import java.util.List;

public class FilterHelper {

	public static List<User> filterByRole(List<User> users, String role) {
		if (role == null || role.isBlank()) return users;

		String roleStr = role.toUpperCase();

		return users.stream()
					.filter(u -> u.getRole() != null &&
								 u.getRole().name().equalsIgnoreCase(roleStr))
					.toList();
	}

	public static List<User> searchUsers(List<User> users, String keyword) {
		if (keyword == null || keyword.isBlank()) return users;

		String lower = keyword.toLowerCase();

		return users.stream()
					.filter(u ->
									(u.getFullName() != null && u.getFullName().toLowerCase().contains(lower))
									||
									(u.getEmail() != null && u.getEmail().toLowerCase().contains(lower))
						   )
					.toList();
	}
}
