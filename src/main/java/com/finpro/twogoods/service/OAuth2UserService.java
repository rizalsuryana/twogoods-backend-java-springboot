package com.finpro.twogoods.service;

import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OAuth2UserService
		extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest request)
			throws OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(request);

		String email = oAuth2User.getAttribute("email");
		String name = oAuth2User.getAttribute("name");
		String sub = oAuth2User.getAttribute("sub");

		User user = userRepository.findByEmail(email)
								  .orElseGet(() -> register(email, name, sub));

		return new DefaultOAuth2User(
				List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
				oAuth2User.getAttributes(),
				"email"
		);
	}

	private User register(String email, String name, String sub) {
		User user = User.builder()
						.email(email)
						.fullName(name)
						.provider("GOOGLE")
						.providerId(sub)
						.role(UserRole.CUSTOMER)
						.build();

		return userRepository.save(user);
	}
}

