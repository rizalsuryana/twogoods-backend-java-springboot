package com.finpro.twogoods.controller;

import com.finpro.twogoods.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/me")
    public ResponseEntity<?> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var response = new HashMap<>();
        response.put("username", authentication.getPrincipal());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

//	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<?> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

}
