package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.request.UserRequest;
import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.dto.response.UserResponse;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.service.UserService;
import com.finpro.twogoods.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name="User")
public class UserController {

	private final UserService userService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String role,
			@RequestParam(required = false) String search
	) {
		Page<UserResponse> users = userService.getAllUsers(page, size, role, search);
		return ResponseUtil.buildPagedResponse(
				HttpStatus.OK,
				"Users fetched successfully",
				users
		);
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserResponse>> getMe() {
		UserResponse me = userService.getMe();
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Current user fetched successfully",
				me
		);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> updateUser(
			@PathVariable Long id,
			@RequestBody UserRequest request,
			Authentication auth
	) {
		User user = (User) auth.getPrincipal();

		if (!user.getId().equals(id)) {
			throw new AccessDeniedException("You can only update your own account");
		}

		User updated = userService.updateUser(id, request);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"User updated successfully",
				updated.toResponse()
		);
	}

	@PutMapping("/{id}/profile-picture")
	public ResponseEntity<ApiResponse<UserResponse>> updateProfilePicture(
			@PathVariable Long id,
			@RequestParam("file") MultipartFile file,
			Authentication auth
	) {
		User user = (User) auth.getPrincipal();

		if (!user.getId().equals(id)) {
			throw new AccessDeniedException("You can only update your own profile picture");
		}

		User updated = userService.updateProfilePicture(id, file);

		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"Profile picture updated successfully",
				updated.toResponse()
		);
	}
}

