package com.finpro.twogoods.service;

import com.finpro.twogoods.entity.Customer;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.exception.ResourceDuplicateException;
import com.finpro.twogoods.model.request.CustomerRegisterRequest;
import com.finpro.twogoods.repository.CustomerRepository;
import com.finpro.twogoods.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder    passwordEncoder;
    private final UserRepository     userRepository;
    private final UserService        userService;

    @Transactional( rollbackFor = Exception.class )
    public Customer createCustomerUser (CustomerRegisterRequest request) {
        if ( userRepository.existsByEmail(request.getEmail()) ) {
            throw new ResourceDuplicateException("Email already exists");
        }

        User user = userService.createUser(request);

        Customer customer = Customer.builder().user(user).fullName(request.getFullName()).build();

        return customerRepository.saveAndFlush(customer);
    }
}
