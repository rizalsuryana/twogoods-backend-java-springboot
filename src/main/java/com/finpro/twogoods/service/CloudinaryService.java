package com.finpro.twogoods.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service @RequiredArgsConstructor @Transactional(readOnly = true) public class CloudinaryService {

	private final Cloudinary cloudinary;

	@Transactional(rollbackFor = Exception.class)
	public String uploadImage(MultipartFile file) {
		try {
			Map upload = cloudinary.uploader().upload(file.getBytes(), Map.of());
			return (String) upload.get("secure_url");
		} catch (Exception e) {
			throw new RuntimeException("Failed to upload image", e);
		}
	}
}
