package com.finpro.twogoods.utils.shedule;

import com.finpro.twogoods.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CancelAutoExpireScheduler {

	private final TransactionRepository transactionRepository;

	// Jalan tiap 1 jam
	@Scheduled(fixedRate = 60 * 60 * 1000) //1jam
//	@Scheduled(fixedRate = 60 * 1000) //1menit
	public void autoExpireCancel() {

		LocalDateTime now = LocalDateTime.now();

		transactionRepository.findAll().forEach(trx -> {

			if (Boolean.TRUE.equals(trx.getCustomerCancelRequest())
					&& !Boolean.TRUE.equals(trx.getMerchantCancelConfirm())
					&& trx.getCancelRequestedAt() != null) {

				// Sudah lebih dari 24 jam
				if (trx.getCancelRequestedAt().plusHours(24).isBefore(now)) {
//				if (trx.getCancelRequestedAt().plusMinutes(1).isBefore(now)) {

					trx.setCustomerCancelRequest(false);
					trx.setCancelRequestedAt(null);

					transactionRepository.save(trx);
				}
			}
		});
	}
}
