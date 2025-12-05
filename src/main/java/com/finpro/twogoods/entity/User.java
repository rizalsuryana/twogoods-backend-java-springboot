package com.finpro.twogoods.entity;

import com.finpro.twogoods.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {
    @Column(nullable = false, unique = true)
    private String username;

    private String password;

	@Column(name = "name", nullable = false)
    private String fullName;

    @Column(unique = true)
    private String email;

	@Column(name = "profile_picture")
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled = true;

	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
	private CustomerProfile customerProfile;

	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
	private MerchantProfile merchantProfile;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getRoleName()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}