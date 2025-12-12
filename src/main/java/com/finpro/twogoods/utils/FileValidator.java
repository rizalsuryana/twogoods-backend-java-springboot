package com.finpro.twogoods.utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileValidator {

	private static final long MAX_SIZE = 2 * 1024 * 1024; // 2 MB

	private static final List<String> ALLOWED_TYPES = List.of(
			"image/jpeg",
			"image/png",
			"image/webp"
	);

	public static void validateImage(MultipartFile file) {

		if (file.isEmpty()) {
			throw new IllegalArgumentException("File cannot be empty");
		}

		if (file.getSize() > MAX_SIZE) {
			throw new IllegalArgumentException("File size exceeds 2 MB limit");
		}

		if (!ALLOWED_TYPES.contains(file.getContentType())) {
			throw new IllegalArgumentException("Only JPG, PNG, and WEBP formats are allowed");
		}
	}
}
