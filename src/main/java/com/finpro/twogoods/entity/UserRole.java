package com.finpro.twogoods.entity;

import lombok.Getter;

public enum UserRole {
	ADMIN("admin"),
	CUSTOMER("customer"),
	MERCHANT("merchant");

	@Getter
	private final String displayName;

	UserRole(String displayName) {
		this.displayName = displayName;
	}

	public String getRoleName() {
		return "ROLE_" + this.name();
	}
}
