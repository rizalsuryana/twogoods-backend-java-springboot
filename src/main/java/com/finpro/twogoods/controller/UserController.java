package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.UserResponse;
import com.finpro.twogoods.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	// /me (ambil user dari token)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/me")
	public ResponseEntity<?> getMe() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		var response = new HashMap<>();
		response.put("username", authentication.getPrincipal());
		response.put("authorities", authentication.getAuthorities());

		return ResponseEntity.ok(response);
	}

	// GET /users (pagination + filter + search)
	// Contoh: /api/v1/users?page=0&size=10&role=ROLE_MERCHANT&search=rizal
//	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String role,
			@RequestParam(required = false) String search
	) {
		return ResponseEntity.ok(userService.getAllUsers(page, size, role, search));
	}
}
