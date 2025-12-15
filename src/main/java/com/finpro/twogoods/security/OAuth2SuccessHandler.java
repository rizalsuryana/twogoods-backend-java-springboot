package com.finpro.twogoods.security;

import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.repository.UserRepository;
import com.finpro.twogoods.service.UserService;
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
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	private final UserRepository userRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
										HttpServletResponse response,
										Authentication authentication) throws IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		// ambil user dari DB atau register baru
		User user = userRepository.findByEmail(oAuth2User.getAttribute("email"))
								  .orElseGet(() -> userService.createCustomerForOAuth(oAuth2User));

		// generate token
		String token = jwtTokenProvider.generateToken(user);

		// redirect ke frontend
		response.sendRedirect("http://localhost:5173/oauth/callback?token=" + token);
	}

}
