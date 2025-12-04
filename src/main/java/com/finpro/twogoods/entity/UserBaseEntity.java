package com.finpro.twogoods.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class UserBaseEntity extends BaseEntity{


	@Column(unique = true)
	private String email;

	private String password;

	@Column(name="profile_picture")
	private String profilePicture;


}
