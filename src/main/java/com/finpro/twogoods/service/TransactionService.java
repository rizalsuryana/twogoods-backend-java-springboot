package com.finpro.twogoods.service;

import com.finpro.twogoods.client.dto.MidtransSnapRequest;
import com.finpro.twogoods.client.dto.MidtransSnapResponse;
import com.finpro.twogoods.dto.response.MerchantSummaryResponse;
import com.finpro.twogoods.dto.response.PagedResult;
import com.finpro.twogoods.dto.response.PagingResponse;
import com.finpro.twogoods.dto.response.TransactionResponse;
import com.finpro.twogoods.entity.*;
import com.finpro.twogoods.enums.OrderStatus;
import com.finpro.twogoods.enums.UserRole;
import com.finpro.twogoods.exceptions.ApiException;
import com.finpro.twogoods.exceptions.ResourceNotFoundException;
import com.finpro.twogoods.repository.MerchantProfileRepository;
import com.finpro.twogoods.repository.MerchantReviewRepository;
import com.finpro.twogoods.repository.ProductRepository;
import com.finpro.twogoods.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.finpro.twogoods.enums.OrderStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TransactionService {

	private final MerchantReviewRepository merchantReviewRepository;
	private final TransactionRepository transactionRepository;
	private final ProductRepository productRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final MidtransService midtransService;

	// Buy now
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse buyNow(Long productId) {
		User user = getCurrentUser();

		String orderId = "ORDER-" + user.getId() + UUID.randomUUID();

		if (!user.getRole().equals(UserRole.CUSTOMER)) {
			throw new ApiException("Only customers can buy products");
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getIsAvailable()) {
			throw new ApiException("Product is sold out");
		}

		if (product.getMerchant().getUser().getId().equals(user.getId())) {
			throw new ApiException("Merchant cannot buy their own product");
		}

		Transaction trx = Transaction.builder()
				.customer(user)
				.orderId(orderId)
				.merchant(product.getMerchant())
				.status(PENDING)
				.totalPrice(product.getPrice())
				.build();

		TransactionItem item = TransactionItem.builder()
				.transaction(trx)
				.product(product)
				.price(product.getPrice())
				.quantity(1)
				.build();

		trx.getItems().add(item);

		Transaction saved = transactionRepository.save(trx);

		product.setIsAvailable(false);
		productRepository.save(product);

		TransactionResponse res = saved.toResponse();

		//Snap Request
		MidtransSnapRequest.TransactionDetails details =
				MidtransSnapRequest
						.TransactionDetails.builder()
						.grossAmount(product.getPrice().intValue())
						.orderId(orderId)
						.build();

		MidtransSnapRequest req = MidtransSnapRequest.builder()
				.transactionDetails(details)
				.callbacks(new MidtransSnapRequest.Callbacks(
						"https://www.2goods.com"))
				.build();
		MidtransSnapResponse midtransResponse = midtransService.createSnap(req);
		log.info("CREATE SNAP WITH ORDER ID: {}", orderId);


		res.setMidtransSnapResponse(midtransResponse);

		return res;
	}

	// GET DETAIL TRANSACTION
	@Transactional(readOnly = true)
	public TransactionResponse getTransactionDetail(Long id) {

		User currentUser = getCurrentUser();

		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		boolean isCustomer = trx.getCustomer().getId().equals(currentUser.getId());
		boolean isMerchant = trx.getMerchant().getUser().getId().equals(currentUser.getId());

		if (!isCustomer && !isMerchant) {
			throw new ApiException("You are not allowed to view this transaction");
		}

		// Ambil response dasar
		TransactionResponse res = trx.toResponse();
		res.setMerchant(buildMerchantSummary(trx.getMerchant()));

		merchantReviewRepository.findByTransactionId(id).ifPresent(review -> {
			res.setAlreadyRated(true);
			res.setReviewId(review.getId());
			res.setRating(review.getRating());
			res.setComment(review.getComment());
		});

		return res;
	}


	// GET CUSTOMER TRANSACTIONS
	public PagedResult<TransactionResponse> getMyTransactions(
			Integer page,
			Integer size,
			OrderStatus status,
			String search,
			String startDate,
			String endDate,
			String sortBy,
			String sortDir
	) {
		User customer = getCurrentUser();

		Pageable pageable = PageRequest.of(
				page,
				size,
				sortDir.equalsIgnoreCase("ASC")
						? Sort.by(sortBy).ascending()
						: Sort.by(sortBy).descending()
		);

		LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
		LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

		Page<Transaction> result = transactionRepository.filterCustomerTransactions(
				customer.getId(),
				status,
				search,
				start,
				end,
				pageable
		);

		PagingResponse paging = PagingResponse.builder()
				.page(page)
				.rowsPerPage(size)
				.totalRows(result.getTotalElements())
				.totalPages(result.getTotalPages())
				.hasNext(result.hasNext())
				.hasPrevious(result.hasPrevious())
				.build();

		return PagedResult.<TransactionResponse>builder()
				.paging(paging)
				.data(result.getContent().stream().map(trx -> {
					TransactionResponse res = trx.toResponse();
					res.setMerchant(buildMerchantSummary(trx.getMerchant()));
					return res;
				}).toList())

				.build();
	}


	// GET MERCHANT ORDERS
	public PagedResult<TransactionResponse> getMerchantOrders(
			Integer page,
			Integer size,
			OrderStatus status,
			String search,
			String startDate,
			String endDate,
			String sortBy,
			String sortDir
	) {
		User merchantUser = getCurrentUser();

		MerchantProfile merchant = merchantProfileRepository.findByUser(merchantUser)
				.orElseThrow(() -> new ApiException("Merchant profile not found"));

		Pageable pageable = PageRequest.of(
				page,
				size,
				sortDir.equalsIgnoreCase("ASC")
						? Sort.by(sortBy).ascending()
						: Sort.by(sortBy).descending()
		);

		LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
		LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

		Page<Transaction> result = transactionRepository.filterMerchantOrders(
				merchant.getId(),
				status,
				search,
				start,
				end,
				pageable
		);

		PagingResponse paging = PagingResponse.builder()
				.page(page)
				.rowsPerPage(size)
				.totalRows(result.getTotalElements())
				.totalPages(result.getTotalPages())
				.hasNext(result.hasNext())
				.hasPrevious(result.hasPrevious())
				.build();

		return PagedResult.<TransactionResponse>builder()
				.paging(paging)
				.data(result.getContent().stream().map(trx -> {
					TransactionResponse res = trx.toResponse();
					res.setMerchant(buildMerchantSummary(trx.getMerchant()));
					return res;
				}).toList())

				.build();
	}

	private PagedResult<TransactionResponse> getTransactionResponsePagedResult(Integer page, Integer rowsPerPage, Page<Transaction> result) {
		PagingResponse paging = PagingResponse.builder()
				.page(page)
				.rowsPerPage(rowsPerPage)
				.totalRows(result.getTotalElements())
				.totalPages(result.getTotalPages())
				.hasNext(result.hasNext())
				.hasPrevious(result.hasPrevious())
				.build();

		return PagedResult.<TransactionResponse>builder()
				.paging(paging)
				.data(result.getContent().stream().map(trx -> {
					TransactionResponse res = trx.toResponse();
					res.setMerchant(buildMerchantSummary(trx.getMerchant()));
					return res;
				}).toList())

				.build();
	}


	// UPDATE STATUS
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse updateStatus(Long id, OrderStatus newStatus) {

		User currentUser = getCurrentUser();

		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		boolean isCustomer = trx.getCustomer().getId().equals(currentUser.getId());
		boolean isMerchant = trx.getMerchant().getUser().getId().equals(currentUser.getId());

		if (!isCustomer && !isMerchant) {
			throw new ApiException("You are not allowed to update this transaction");
		}

		OrderStatus currentStatus = trx.getStatus();

		switch (newStatus) {

			case PAID:
				throw new ApiException(
						"PAID status is managed by Midtrans"
				);
			case PACKING:
				if (!isMerchant) throw new ApiException("Only merchant can set PACKING");
				if (currentStatus != PAID) {
					throw new ApiException("PACKING can only be set from PAID");
				}
				break;

			case SHIPPED:
				if (!isMerchant) throw new ApiException("Only merchant can set SHIPPED");
				if (currentStatus != PACKING) {
					throw new ApiException("SHIPPED can only be set from PACKING");
				}
				break;

			case DELIVERING:
				if (!isMerchant) throw new ApiException("Only merchant can set DELIVERING");
				if (currentStatus != SHIPPED) {
					throw new ApiException("DELIVERING can only be set from SHIPPED");
				}
				break;

			case COMPLETED:
				if (!isCustomer) throw new ApiException("Only customer can set COMPLETED");
				if (currentStatus != DELIVERING) {
					throw new ApiException("COMPLETED can only be set from DELIVERING");
				}
				break;

			case CANCELED:
				if (currentStatus == SHIPPED
						|| currentStatus == DELIVERING
						|| currentStatus == COMPLETED) {
					throw new ApiException("Cannot cancel after item is shipped");
				}
				break;

			default:
				throw new ApiException("Invalid status update");
		}

		trx.setStatus(newStatus);
		Transaction updated = transactionRepository.save(trx);

		return updated.toResponse();
	}

	//	Req cancel si cust
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse requestCancel(Long id) {
		User user = getCurrentUser();
		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getCustomer().getId().equals(user.getId())) {
			throw new ApiException("Only customer can request cancel");
		}

		if (trx.getStatus() == SHIPPED
				|| trx.getStatus() == DELIVERING
				|| trx.getStatus() == COMPLETED) {
			throw new ApiException("Cannot cancel after shipped");
		}

		trx.setCustomerCancelRequest(true);
//		counting wkwk
		trx.setCancelRequestedAt(LocalDateTime.now());

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}

	//merch confirm cancel
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse confirmCancel(Long id) {
		User user = getCurrentUser();
		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getMerchant().getUser().getId().equals(user.getId())) {
			throw new ApiException("Only merchant can confirm cancel");
		}

		if (!Boolean.TRUE.equals(trx.getCustomerCancelRequest())) {
			throw new ApiException("Customer has not requested cancel");
		}

		trx.setMerchantCancelConfirm(true);
		trx.setStatus(OrderStatus.CANCELED);

		if (trx.getStatus() == PENDING) {
			midtransService.directRefund(trx.getOrderId(), trx.getTotalPrice().intValue());
		}
		if (trx.getStatus() == PAID) {
			midtransService.refund(trx.getOrderId(), trx.getTotalPrice().intValue());
		}

		// balikin stock/availability product
		trx.getItems().forEach(item -> {
			Product p = item.getProduct();
			p.setIsAvailable(true);
			productRepository.save(p);
		});

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}


	//cust req return
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse requestReturn(Long id) {
		User user = getCurrentUser();
		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getCustomer().getId().equals(user.getId())) {
			throw new ApiException("Only customer can request return");
		}

		if (trx.getStatus() != OrderStatus.COMPLETED) {
			throw new ApiException("Return is only allowed after COMPLETED");
		}

		trx.setCustomerReturnRequest(true);
//		time for auto cancel
		trx.setReturnRequestedAt(LocalDateTime.now());

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}

	//merchant confirm return
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse confirmReturn(Long id) {
		User user = getCurrentUser();
		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getMerchant().getUser().getId().equals(user.getId())) {
			throw new ApiException("Only merchant can confirm return");
		}

		if (!Boolean.TRUE.equals(trx.getCustomerReturnRequest())) {
			throw new ApiException("Customer has not requested return");
		}

		if (trx.getStatus() == PENDING) {
			midtransService.directRefund(trx.getOrderId(), trx.getTotalPrice().intValue());
		}
		if (trx.getStatus() == PAID) {
			midtransService.refund(trx.getOrderId(), trx.getTotalPrice().intValue());
		}

		trx.setMerchantReturnConfirm(true);
		trx.setStatus(OrderStatus.RETURNED);

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}


	private User getCurrentUser() {
		return (User) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();
	}


	//	Reject Cancel
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse rejectCancel(Long id) {
		User user = getCurrentUser();
		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getMerchant().getUser().getId().equals(user.getId())) {
			throw new ApiException("Only merchant can reject cancel");
		}

		if (!Boolean.TRUE.equals(trx.getCustomerCancelRequest())) {
			throw new ApiException("No cancel request to reject");
		}

		trx.setCustomerCancelRequest(false);
		trx.setMerchantCancelConfirm(false);

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}


	//Reject Return
	@Transactional(rollbackFor = Exception.class)
	public TransactionResponse rejectReturn(Long id) {
		User user = getCurrentUser();
		Transaction trx = transactionRepository.findById(id)
				.orElseThrow(() -> new ApiException("Transaction not found"));

		if (!trx.getMerchant().getUser().getId().equals(user.getId())) {
			throw new ApiException("Only merchant can reject return");
		}

		if (!Boolean.TRUE.equals(trx.getCustomerReturnRequest())) {
			throw new ApiException("No return request to reject");
		}

		trx.setCustomerReturnRequest(false);
		trx.setMerchantReturnConfirm(false);

		Transaction saved = transactionRepository.save(trx);
		return saved.toResponse();
	}


	private MerchantSummaryResponse buildMerchantSummary(MerchantProfile mp) {
		Float avg = merchantReviewRepository.getAverageRating(mp.getId());
		Long total = merchantReviewRepository.getTotalReviews(mp.getId());

		return MerchantSummaryResponse.builder()
				.id(mp.getId())
				.fullName(mp.getUser().getFullName())
				.email(mp.getUser().getEmail())
				.profilePicture(mp.getUser().getProfilePicture())
				.location(mp.getLocation())
				.rating(avg != null ? avg : 0f)
				.totalReviews(total != null ? total : 0L)
				.build();
	}

}
