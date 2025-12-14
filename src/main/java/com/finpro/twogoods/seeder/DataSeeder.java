package com.finpro.twogoods.seeder;

import com.finpro.twogoods.entity.*;
import com.finpro.twogoods.enums.*;
import com.finpro.twogoods.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Transactional
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final CustomerProfileRepository customerProfileRepository;
	private final ProductRepository productRepository;
	private final MerchantReviewRepository reviewRepository;
	private final TransactionRepository transactionRepository;
	private final PasswordEncoder passwordEncoder;

	private final Random random = new Random();

	@Override
	public void run(String... args) throws Exception {

		if (userRepository.count() > 1) return;

		// CUSTOMER
		User c1 = createUser("customer1@mail.com", "cust1", "Customer One", UserRole.CUSTOMER);
		User c2 = createUser("customer2@mail.com", "cust2", "Customer Two", UserRole.CUSTOMER);
		User c3 = createUser("customer3@mail.com", "cust3", "Customer Three", UserRole.CUSTOMER);

		customerProfileRepository.saveAll(List.of(
				CustomerProfile.builder().user(c1).build(),
				CustomerProfile.builder().user(c2).build(),
				CustomerProfile.builder().user(c3).build()
		));

		// MERCHANTS
		User m1 = createUser("merchant1@mail.com", "merch1", "Merchant One", UserRole.MERCHANT);
		User m2 = createUser("merchant2@mail.com", "merch2", "Merchant Two", UserRole.MERCHANT);
		User m3 = createUser("merchant3@mail.com", "merch3", "Merchant Three", UserRole.MERCHANT);

		// 2 merchant ACCEPTED
		MerchantProfile mp1 = MerchantProfile.builder()
				.user(m1)
				.location("Jakarta")
				.NIK("1111111111111111")
				.ktpPhoto(null)
				.isVerified(MerchantStatus.ACCEPTED)
				.rejectReason(null)
				.build();

		MerchantProfile mp2 = MerchantProfile.builder()
				.user(m2)
				.location("Bandung")
				.NIK("2222222222222222")
				.ktpPhoto(null)
				.isVerified(MerchantStatus.ACCEPTED)
				.rejectReason(null)
				.build();

		// 1 merchant PENDING (tidak boleh jualan)
		MerchantProfile mp3 = MerchantProfile.builder()
				.user(m3)
				.location("Surabaya")
				.NIK("3333333333333333")
				.ktpPhoto(null)
				.isVerified(MerchantStatus.PENDING)
				.rejectReason(null)
				.build();

		merchantProfileRepository.saveAll(List.of(mp1, mp2, mp3));

		// Produk hanya untuk merchant ACCEPTED
		createProductsForMerchant(mp1, "Vintage Shirt");
		createProductsForMerchant(mp2, "Sneakers");

		// Merchant mp3 tidak dibuatkan produk karena status PENDING

		// Transaksi dan rating
		createDummyTransactionAndRating(c1, mp1);
		createDummyTransactionAndRating(c2, mp2);
		createDummyTransactionAndRating(c3, mp3);

		System.out.println("Seeder selesai");
	}

	private User createUser(String email, String username, String name, UserRole role) {
		User user = User.builder()
				.email(email)
				.username(username)
				.fullName(name)
				.password(passwordEncoder.encode("password"))
				.role(role)
				.enabled(true)
				.build();
		return userRepository.save(user);
	}

	private void createProductsForMerchant(MerchantProfile merchant, String baseName) {

		if (merchant.getIsVerified() != MerchantStatus.ACCEPTED) {
			return;
		}

		Categories[] allCategories = Categories.values();

		for (int i = 1; i <= 5; i++) {

			Categories randomCategory = allCategories[random.nextInt(allCategories.length)];

			Product p = Product.builder()
					.merchant(merchant)
					.name(baseName + " " + i)
					.description("Deskripsi produk " + baseName + " nomor " + i)
					.price(BigDecimal.valueOf(50000 + (i * 10000)))
					.categories(List.of(randomCategory))
					.color("random")
					.isAvailable(true)
					.condition(ProductCondition.USED)
					.build();

			productRepository.save(p);
		}
	}

	private void createDummyTransactionAndRating(User customer, MerchantProfile merchant) {

		Product product = productRepository.findFirstByMerchant(merchant);
		if (product == null) return;

		Transaction trx = Transaction.builder()
				.customer(customer)
				.merchant(merchant)
				.status(OrderStatus.COMPLETED)
				.totalPrice(product.getPrice())
				.build();

		TransactionItem item = TransactionItem.builder()
				.transaction(trx)
				.product(product)
				.price(product.getPrice())
				.quantity(1)
				.build();

		trx.getItems().add(item);
		transactionRepository.save(trx);

		MerchantReview review = MerchantReview.builder()
				.merchant(merchant)
				.user(customer)
				.transaction(trx)
				.rating(3f + random.nextFloat() * 2f)
				.comment("Produk bagus, recommended")
				.build();

		reviewRepository.save(review);
	}
}
