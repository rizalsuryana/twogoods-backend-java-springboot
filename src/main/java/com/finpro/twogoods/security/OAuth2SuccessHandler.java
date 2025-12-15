package com.finpro.twogoods.security;

import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
		extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtService;
	private final UserRepository userRepository;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
									   ) throws IOException {

		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		String email = oAuth2User.getAttribute("email");

		User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		String token = jwtService.generateToken(user);

		response.sendRedirect(
				"http://localhost:5173/oauth/callback?token=" + token
							 );
	}
}

