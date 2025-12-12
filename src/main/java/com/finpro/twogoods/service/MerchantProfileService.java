package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.UserRequest;
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
	private final UserService userService;

	public MerchantProfileResponse getMerchantById(Long id) {
		MerchantProfile profile = merchantProfileRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

		return buildResponse(profile);
	}

	@Transactional(rollbackFor = Exception.class)
	public MerchantProfileResponse updateMerchantProfile(Long id, MerchantProfile merchantProfile) {

		MerchantProfile profile = merchantProfileRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));

		UserRequest userRequest = UserRequest.builder()
											 .profilePicture(merchantProfile.getUser().getProfilePicture())
											 .email(merchantProfile.getUser().getEmail())
											 .password(merchantProfile.getUser().getPassword())
											 .fullName(merchantProfile.getUser().getFullName())
											 .username(merchantProfile.getUser().getUsername())
											 .build();

		userService.updateUser(merchantProfile.getUser().getId(), userRequest);

		profile.setLocation(merchantProfile.getLocation());
		profile.setNIK(merchantProfile.getNIK());

		MerchantProfile saved = merchantProfileRepository.save(profile);

		return buildResponse(saved);
	}

	public Page<MerchantProfileResponse> getAllPaginated(Pageable pageable) {
		Page<MerchantProfile> profiles = merchantProfileRepository.findAll(pageable);

		return profiles.map(this::buildResponse);
	}

	public List<MerchantProfileResponse> getAllMerchantProfiles() {
		return merchantProfileRepository.findAll().stream()
				.map(this::buildResponse)
				.toList();
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteMerchantProfileById(Long id) {
		MerchantProfile profile = merchantProfileRepository.findById(id)
														   .orElseThrow(() -> new ResourceNotFoundException(
																   "Merchant not found"));

		merchantProfileRepository.delete(profile);
	}

	private MerchantProfileResponse buildResponse(MerchantProfile profile) {
		Double avg = merchantReviewRepository.getAverageRating(profile.getId());
		Long total = merchantReviewRepository.getTotalReviews(profile.getId());

		MerchantProfileResponse response = profile.toResponse();
		response.setRating(avg != null ? avg : 0);
		response.setTotalReviews(total);

		return response;
	}
}

