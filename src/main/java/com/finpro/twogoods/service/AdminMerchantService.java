package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.response.MerchantProfileResponse;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class AdminMerchantService {

	private final MerchantProfileRepository merchantProfileRepository;

	public List<MerchantProfileResponse> getAll() {
		return merchantProfileRepository.findAll()
				.stream()
				.map(MerchantProfile::toResponse)
				.toList();
	}

	public List<MerchantProfileResponse> getPending() {
		return merchantProfileRepository.findByIsVerifiedFalse()
				.stream()
				.map(MerchantProfile::toResponse)
				.toList();
	}

	public List<MerchantProfileResponse> getVerified() {
		return merchantProfileRepository.findByIsVerifiedTrue()
				.stream()
				.map(MerchantProfile::toResponse)
				.toList();
	}

	@Transactional(rollbackFor = Exception.class)
	public void verify(Long merchantId) {
		MerchantProfile merchant = merchantProfileRepository.findById(merchantId)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

		if (merchant.getNIK() == null || merchant.getKtpPhoto() == null) {
			throw new ApiException("Merchant has not submitted NIK or KTP photo");
		}

		merchant.setIsVerified(true);
		merchant.setRejectReason(null);
	}

	@Transactional(rollbackFor = Exception.class)
	public void reject(Long merchantId, String reason) {
		MerchantProfile merchant = merchantProfileRepository.findById(merchantId)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

		merchant.setIsVerified(false);
		merchant.setRejectReason(reason);
	}

	@Transactional(rollbackFor = Exception.class)
	public void disable(Long merchantId) {
		MerchantProfile merchant = merchantProfileRepository.findById(merchantId)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

		merchant.getUser().setEnabled(false);
	}

	@Transactional(rollbackFor = Exception.class)
	public void enable(Long merchantId) {
		MerchantProfile merchant = merchantProfileRepository.findById(merchantId)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

		merchant.getUser().setEnabled(true);
	}

}
