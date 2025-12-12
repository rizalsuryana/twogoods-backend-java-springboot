package com.finpro.twogoods.service;

import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.entity.User;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import com.finpro.twogoods.utils.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
@Service
@RequiredArgsConstructor
public class MerchantSelfService {

	private final MerchantProfileRepository merchantProfileRepository;
	private final UserService userService;
	private final CloudinaryService cloudinaryService;

	@Transactional(rollbackFor = Exception.class)
	public void uploadKtp(MultipartFile file) {

		User user = userService.getUserById(userService.getMe().getId());

		MerchantProfile merchant = merchantProfileRepository.findById(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Merchant profile not found"));

		if (merchant.getNIK() == null) {
			throw new ApiException("Please fill NIK in your profile before uploading KTP");
		}

		FileValidator.validateImage(file);

		String folder = "merchant_ktp/" + user.getId();
		String ktpUrl = cloudinaryService.uploadImage(file, folder);

		merchant.setKtpPhoto(ktpUrl);
		merchant.setIsVerified(false);
		merchant.setRejectReason(null);

		merchantProfileRepository.save(merchant);
	}
}
