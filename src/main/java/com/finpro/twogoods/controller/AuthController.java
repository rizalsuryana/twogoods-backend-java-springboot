package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.request.CustomerRegisterRequest;
import com.finpro.twogoods.dto.request.LoginRequest;
import com.finpro.twogoods.dto.request.MerchantRegisterRequest;
import com.finpro.twogoods.service.AuthService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auths")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> loginHandler(@Valid @RequestBody LoginRequest request) {
        return ResponseUtil.buildSingleResponse(
                HttpStatus.CREATED,
                HttpStatus.CREATED.getReasonPhrase(),
                authService.login(request)
        );
    }

	@PostMapping("/register/customer")
	public ResponseEntity<?> registerCustomer(
			@Valid @RequestBody CustomerRegisterRequest request
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.CREATED,
				"Customer registered",
				authService.registerCustomer(request)
		);
	}


	@PostMapping("/register/merchant")
	public ResponseEntity<?> registerMerchant(
			@Valid @RequestBody MerchantRegisterRequest request
	) {
		return ResponseUtil.buildSingleResponse(
				HttpStatus.CREATED,
				"Merchant registered",
				authService.registerMerchant(request)
		);
	}



}
