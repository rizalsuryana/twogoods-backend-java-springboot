package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.MerchantProfileUpdateRequest;
import com.finpro.twogoods.dto.response.MerchantProfileResponse;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import com.finpro.twogoods.repository.MerchantReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MerchantProfileService {

	private final MerchantProfileRepository merchantProfileRepository;
	private final MerchantReviewRepository merchantReviewRepository;

	public MerchantProfileResponse getMerchantById(Long id) {
		MerchantProfile profile = merchantProfileRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

		return buildResponse(profile);
	}

	@Transactional(rollbackFor = Exception.class)
	public MerchantProfileResponse updateMerchantProfile(Long id, MerchantProfileUpdateRequest request) {

		MerchantProfile profile = merchantProfileRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

		if (request.getNik() != null) {
			profile.setNIK(request.getNik());
		}

		if (request.getLocation() != null) {
			profile.setLocation(request.getLocation());
		}

		MerchantProfile saved = merchantProfileRepository.save(profile);

		return buildResponse(saved);
	}

	public Page<MerchantProfileResponse> getAllPaginated(Pageable pageable) {
		return merchantProfileRepository.findAll(pageable)
				.map(this::buildResponse);
	}

	public List<MerchantProfileResponse> getAllMerchantProfiles() {
		return merchantProfileRepository.findAll().stream()
				.map(this::buildResponse)
				.toList();
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteMerchantProfileById(Long id) {
		MerchantProfile profile = merchantProfileRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

		merchantProfileRepository.delete(profile);
	}

	private MerchantProfileResponse buildResponse(MerchantProfile profile) {
		Float avg = merchantReviewRepository.getAverageRating(profile.getId());
		Long total = merchantReviewRepository.getTotalReviews(profile.getId());

		Float formatted = avg != null ? Math.round(avg * 10f) / 10f : 0f;

		MerchantProfileResponse response = profile.toResponse();
		response.setRating(formatted);
		response.setTotalReviews(total);

		return response;
	}
}
