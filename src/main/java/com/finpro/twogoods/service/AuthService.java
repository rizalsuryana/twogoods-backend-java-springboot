package com.finpro.twogoods.service;

import com.finpro.twogoods.entity.*;
import com.finpro.twogoods.model.request.*;
import com.finpro.twogoods.model.response.LoginResponse;
import com.finpro.twogoods.repository.UserRepository;
import com.finpro.twogoods.security.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${twogoods.admin.username}")
    private String ADMIN_USERNAME;

    @Value("${twogoods.admin.password}")
    private String ADMIN_PASSWORD;

    public LoginResponse loginCustomer(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );

        Customer customer = (Customer ) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(customer.getUser());
        return LoginResponse.builder()
                .accessToken(token)
                .user(customer.toUserResponse())
                .build();
    }

    public LoginResponse loginMerchant(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );

        Merchant merchant = (Merchant) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(merchant.getUser());

        return LoginResponse.builder()
                            .accessToken(token)
                            .user(merchant.toUserResponse())
                            .build();
    }


    @PostConstruct
    public void adminSeeder() {
        if(userRepository.existsByEmail(ADMIN_USERNAME)){
            return;
        }

        userRepository.save(User.builder()
                                .email(ADMIN_USERNAME)
                                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                                .role(UserRole.ADMIN)
                                .enabled(true)
                                .build()
        );
    }
}
