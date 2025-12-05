package com.finpro.twogoods.controller;

import com.finpro.twogoods.entity.Customer;
import com.finpro.twogoods.model.request.CustomerRegisterRequest;
import com.finpro.twogoods.model.response.RegisterResponse;
import com.finpro.twogoods.repository.CustomerRepository;
import com.finpro.twogoods.service.CustomerService;
import com.finpro.twogoods.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody CustomerRegisterRequest payload) {
        RegisterResponse response = customerService.createCustomerUser(payload).toRegisterResponse();
        return ResponseUtil.buildSingleResponse(HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase(), response);
    }


}
