//package com.finpro.twogoods.seeder;
//
//import com.finpro.twogoods.entity.*;
//import com.finpro.twogoods.enums.ProductCondition;
//import com.finpro.twogoods.enums.UserRole;
//import com.finpro.twogoods.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.util.List;
//
////@Configuration
//@Component
//@RequiredArgsConstructor
//public class DataSeeder implements CommandLineRunner {
//
//	private final UserRepository userRepository;
//	private final MerchantProfileRepository merchantProfileRepository;
//	private final CustomerProfileRepository customerProfileRepository;
//	private final ProductRepository productRepository;
//	private final PasswordEncoder passwordEncoder;
//
//	@Override
//	public void run(String... args) throws Exception {
//
//		// Cegah duplikasi seeder
//		if (userRepository.count() > 1) {
//			return;
//		}
//
////customer x3
//		User c1 = createUser("customer1@mail.com", "cust1", "Customer One", UserRole.CUSTOMER);
//		User c2 = createUser("customer2@mail.com", "cust2", "Customer Two", UserRole.CUSTOMER);
//		User c3 = createUser("customer3@mail.com", "cust3", "Customer Three", UserRole.CUSTOMER);
//
//		customerProfileRepository.saveAll(List.of(
//				CustomerProfile.builder().user(c1).location("Jakarta").build(),
//				CustomerProfile.builder().user(c2).location("Bandung").build(),
//				CustomerProfile.builder().user(c3).location("Surabaya").build()
//		));
//
////	merchant x3
//		User m1 = createUser("merchant1@mail.com", "merch1", "Merchant One", UserRole.MERCHANT);
//		User m2 = createUser("merchant2@mail.com", "merch2", "Merchant Two", UserRole.MERCHANT);
//		User m3 = createUser("merchant3@mail.com", "merch3", "Merchant Three", UserRole.MERCHANT);
//
//		MerchantProfile mp1 = MerchantProfile.builder().user(m1).location("Jakarta").NIK("111111").rating(5).build();
//		MerchantProfile mp2 = MerchantProfile.builder().user(m2).location("Bandung").NIK("222222").rating(4).build();
//		MerchantProfile mp3 = MerchantProfile.builder().user(m3).location("Surabaya").NIK("333333").rating(5).build();
//
//		merchantProfileRepository.saveAll(List.of(mp1, mp2, mp3));
//
////	user x3
//		createProductsForMerchant(mp1, "Vintage Shirt");
//		createProductsForMerchant(mp2, "Sneakers");
//		createProductsForMerchant(mp3, "Hoodie");
//
//		System.out.println("✅ Data seeder berhasil dijalankan!");
//	}
//
//	private User createUser(String email, String username, String name, UserRole role) {
//		User user = User.builder()
//				.email(email)
//				.username(username)
//				.fullName(name)
//				.password(passwordEncoder.encode("password"))
//				.role(role)
//				.enabled(true)
//				.build();
//		return userRepository.save(user);
//	}
//
//	private void createProductsForMerchant(MerchantProfile merchant, String baseName) {
//		for (int i = 1; i <= 5; i++) {
//			Product p = Product.builder()
//					.merchant(merchant)
//					.name(baseName + " " + i)
//					.description("Deskripsi produk " + baseName + " nomor " + i)
//					.price(BigDecimal.valueOf(50000 + (i * 10000)))
//					.category("fashion")
//					.color("random")
//					.isAvailable(true)
//					.condition(ProductCondition.USED)
//					.build();
//
//			productRepository.save(p);
//		}
//	}
//}
