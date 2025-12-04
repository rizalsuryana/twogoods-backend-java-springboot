package com.finpro.twogoods.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {

	@Column(nullable = false, unique = true)
	private String username; //email from student

	private String password;

	@Enumerated(EnumType.STRING)
	private UserRole role;

	@Column(nullable = false)
	private boolean enabled = true;



	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role.getRoleName()));
	}


	@Override
	public boolean isAccountNonExpired() {
//		return UserDetails.super.isAccountNonExpired();
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
//		return UserDetails.super.isAccountNonLocked();
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
//		return UserDetails.super.isCredentialsNonExpired();
		return true;
	}

	@Override
	public boolean isEnabled() {
//		untuk user verif email / baned
//		return UserDetails.super.isEnabled();
//		return isEnabled();
		return enabled;
	}
}
