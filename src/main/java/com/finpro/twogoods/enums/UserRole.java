package com.finpro.twogoods.enums;

import lombok.Getter;

public enum UserRole {
	ADMIN("ADMIN"),
	MERCHANT("MERCHANT"),
	CUSTOMER("CUSTOMER");

    @Getter
    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}
