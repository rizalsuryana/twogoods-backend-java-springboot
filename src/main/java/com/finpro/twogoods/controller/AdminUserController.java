package com.finpro.twogoods.controller;

import com.finpro.twogoods.dto.response.ApiResponse;
import com.finpro.twogoods.service.AdminUserService;
import com.finpro.twogoods.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

	private final AdminUserService adminUserService;

	@PatchMapping("/{id}/disable")
	public ResponseEntity<ApiResponse<Object>> disableUser(@PathVariable Long id) {
		adminUserService.disableUser(id);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"User disabled",
				null
		);
	}

	@PatchMapping("/{id}/enable")
	public ResponseEntity<ApiResponse<Object>> enableUser(@PathVariable Long id) {
		adminUserService.enableUser(id);
		return ResponseUtil.buildSingleResponse(
				HttpStatus.OK,
				"User enabled",
				null
		);
	}
}

