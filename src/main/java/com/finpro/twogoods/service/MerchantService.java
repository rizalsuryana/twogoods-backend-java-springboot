package com.finpro.twogoods.service;

import com.finpro.twogoods.entity.Merchant;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.entity.UserRole;
import com.finpro.twogoods.exception.ResourceDuplicateException;
import com.finpro.twogoods.model.request.MerchantRegisterRequest;
import com.finpro.twogoods.repository.MerchantRepository;
import com.finpro.twogoods.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantService {
    private final MerchantRepository merchantRepository;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public Merchant createMerchant(MerchantRegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceDuplicateException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.MERCHANT)
                .enabled(true)
                .build();

        Merchant merchant = Merchant.builder()
                                    .merchantName(request.getMerchantName())
                                    .user(user)
                                    .build();

        return merchantRepository.saveAndFlush(merchant);
    }


}
