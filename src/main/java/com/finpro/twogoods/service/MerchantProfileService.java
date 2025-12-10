package com.finpro.twogoods.service;

import com.finpro.twogoods.dto.request.UserRequest;
import com.finpro.twogoods.entity.MerchantProfile;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.MerchantProfileRepository;
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
	private final UserService userService;

	public MerchantProfile getMerchantById(Long id) {
		return merchantProfileRepository.findById(id)
										.orElseThrow(() -> new ResourceNotFoundException("Merchant not found"));
	}

	@Transactional(rollbackFor = Exception.class)
	public MerchantProfile updateMerchantProfile(Long id, MerchantProfile merchantProfile) {

		MerchantProfile profile = getMerchantById(id);

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

		return merchantProfileRepository.save(profile);
	}

	public Page<MerchantProfile> getAllPaginated(Pageable pageable) {
		return merchantProfileRepository.findAll(pageable);
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteMerchantProfileById(Long id) {
		MerchantProfile profile = merchantProfileRepository.findById(id)
														   .orElseThrow(() -> new ResourceNotFoundException(
																   "Merchant not found"));

		merchantProfileRepository.delete(profile);
	}

	public List<MerchantProfile> getAllMerchantProfiles() {
		return merchantProfileRepository.findAll();
	}
}
