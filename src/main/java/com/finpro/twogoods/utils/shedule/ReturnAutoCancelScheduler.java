package com.finpro.twogoods.utils.shedule;

import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReturnAutoCancelScheduler {

	private final TransactionRepository transactionRepository;

	// Jalan tiap 1 jam
	@Scheduled(fixedRate = 60 * 60 * 1000)
	public void autoCancelReturn() {

		LocalDateTime now = LocalDateTime.now();

		transactionRepository.findAll().forEach(trx -> {

			if (Boolean.TRUE.equals(trx.getCustomerReturnRequest())
					&& !Boolean.TRUE.equals(trx.getMerchantReturnConfirm())
					&& trx.getStatus() == OrderStatus.COMPLETED
					&& trx.getReturnRequestedAt() != null) {

				// Sudah lebih dari 24 jam
				if (trx.getReturnRequestedAt().plusHours(24).isBefore(now)) {

					trx.setCustomerReturnRequest(false);
					trx.setReturnRequestedAt(null);

					transactionRepository.save(trx);
				}
			}
		});
	}
}
