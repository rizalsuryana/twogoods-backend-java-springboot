//package com.finpro.twogoods.seeder;
//
//import com.finpro.twogoods.entity.seed.SeedHistory;
//import com.finpro.twogoods.repository.seed.SeedHistoryRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Component
//@RequiredArgsConstructor
//public class DatabaseSeeder implements ApplicationRunner {
//
//	private final JdbcTemplate jdbcTemplate;
//	private final UserSeed userSeed;
//	private final CustomerSeed customerSeed;
//	private final MerchantSeed merchantSeed;
//	private final SeedHistoryRepository seedHistoryRepository;
//
//	@Override
//	public void run(ApplicationArguments args) {
//		if (seedHistoryRepository.existsBySeedName("INITIAL_SEED")) {
//			return;
//		}
//
//		seedHistoryRepository.save(SeedHistory.builder()
//											  .seedName("INITIAL_SEED")
//											  .executedAt(LocalDateTime.now())
//											  .build());
//		seed("V1_USERS", userSeed::seed);
//		seed("V2_CUSTOMERS", customerSeed::seed);
//		seed("V3_MERCHANTS", merchantSeed::seed);
//	}
//
//	private void seed(String name, Runnable action) {
//		Integer count = jdbcTemplate.queryForObject(
//				"SELECT COUNT(*) FROM seed_history WHERE seed_name = ?",
//				Integer.class,
//				name
//												   );
//
//		if (count != null && count > 0) {
//			System.out.println("⏭ " + name + " already seeded");
//			return;
//		}
//
//		action.run();
//
//		jdbcTemplate.update(
//				"INSERT INTO seed_history VALUES (?, now())",
//				name
//						   );
//
//		System.out.println("✅ " + name + " seeded");
//	}
//}
//
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class DatabaseSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final MerchantProfileRepository merchantProfileRepository;
	private final CustomerProfileRepository customerProfileRepository;
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final MerchantReviewRepository reviewRepository;
	private final TransactionRepository transactionRepository;
	private final PasswordEncoder passwordEncoder;

	private final Random random = new Random();

	@Override
	public void run(String... args) throws Exception {

		if (userRepository.count() > 1) return;

		// CREATE 10 CUSTOMERS

		List<User> customers = new ArrayList<>();

		for (int i = 1; i <= 10; i++) {
			User c = createUser(
					"customer" + i + "@mail.com",
					"cust" + i,
					"Customer " + i,
					UserRole.CUSTOMER
			);
			customers.add(c);
		}

		customerProfileRepository.saveAll(
				customers.stream()
						.map(c -> CustomerProfile.builder().user(c).build())
						.toList()
		);

		// CREATE 10 MERCHANTS
		List<MerchantProfile> merchants = new ArrayList<>();

		for (int i = 1; i <= 10; i++) {

			User m = createUser(
					"merchant" + i + "@mail.com",
					"merch" + i,
					"Merchant " + i,
					UserRole.MERCHANT
			);

			MerchantStatus status = (i <= 6)
					? MerchantStatus.ACCEPTED
					: MerchantStatus.PENDING;

			MerchantProfile mp = MerchantProfile.builder()
					.user(m)
					.location("City " + i)
					.NIK("99999999999999" + i)
					.isVerified(status)
					.build();

			merchants.add(mp);
		}

		merchantProfileRepository.saveAll(merchants);

		// CREATE 15 PRODUCTS FOR ACCEPTED MERCHANTS
		merchants.stream()
				.filter(m -> m.getIsVerified() == MerchantStatus.ACCEPTED)
				.forEach(this::createProductsForMerchant);

		// CREATE TRANSACTIONS FOR ALL STATUS
		for (User customer : customers) {
			for (MerchantProfile merchant : merchants) {
				if (merchant.getIsVerified() == MerchantStatus.ACCEPTED) {
					createAllTransactionStatuses(customer, merchant);
				}
			}
		}

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

	// CREATE 15 PRODUCTS + IMAGES
	private void createProductsForMerchant(MerchantProfile merchant) {

		String[] names = {
				"Oversized Hoodie", "Casual T-Shirt", "Slim Fit Jeans", "Denim Jacket",
				"Flannel Shirt", "Running Shoes", "Baseball Cap", "Leather Wallet",
				"Canvas Tote Bag", "Windbreaker Jacket", "Chino Pants", "Polo Shirt",
				"Beanie Hat", "Sport Shorts", "Long Sleeve Tee"
		};

		String[] colors = {
				"Black", "White", "Blue", "Red", "Green", "Grey", "Brown", "Navy", "Cream"
		};

		Categories[] categories = {
				Categories.Men, Categories.Women, Categories.Unisex,
				Categories.Shirt, Categories.Pants,
				 Categories.Accessory, Categories.Baby, Categories.Child
		};

		for (int i = 0; i < 15; i++) {

			String name = names[i];
			String color = colors[random.nextInt(colors.length)];
			Categories category = categories[random.nextInt(categories.length)];

			Product p = Product.builder()
					.merchant(merchant)
					.name(name)
					.description("Produk " + name + " berkualitas tinggi, cocok untuk penggunaan harian.")
					.price(BigDecimal.valueOf(50000 + random.nextInt(200000)))
					.categories(List.of(category))
					.color(color)
					.isAvailable(true)
					.condition(random.nextBoolean() ? ProductCondition.NEW : ProductCondition.USED)
					.build();

			productRepository.save(p);

			// Tambahkan 1–3 foto dummy
			int imageCount = 1 + random.nextInt(3);

			for (int j = 1; j <= imageCount; j++) {
				ProductImage img = ProductImage.builder()
						.product(p)
						.imageUrl("https://dummyimage.com/600x600/" +
								Integer.toHexString(random.nextInt(0xFFFFFF)) +
								"/ffffff&text=" + name.replace(" ", "+") + "+" + j)
						.build();

				productImageRepository.save(img);
			}
		}
	}

	// CREATE TRANSACTIONS FOR ALL STATUS
	private void createAllTransactionStatuses(User customer, MerchantProfile merchant) {

		List<OrderStatus> statuses = List.of(
				OrderStatus.PENDING,
				OrderStatus.PACKING,
				OrderStatus.SHIPPED,
				OrderStatus.COMPLETED,
				OrderStatus.CANCELED
		);

		Product product = productRepository.findFirstByMerchant(merchant);
		if (product == null) return;

		for (OrderStatus status : statuses) {

			String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

			Transaction trx = Transaction.builder()
					.orderId(orderId)
					.customer(customer)
					.merchant(merchant)
					.status(status)
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

			if (status == OrderStatus.COMPLETED) {
				MerchantReview review = MerchantReview.builder()
						.merchant(merchant)
						.user(customer)
						.transaction(trx)
						.rating(4f + random.nextFloat())
						.comment("Produk bagus, recommended!")
						.build();

				reviewRepository.save(review);
			}
		}
	}

}
