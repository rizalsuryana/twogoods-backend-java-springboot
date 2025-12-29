//package com.finpro.twogoods.seeder;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class UserSeed {
//
//	private final JdbcTemplate jdbcTemplate;
//	private final PasswordEncoder passwordEncoder;
//
//	public void seed() {
//
//		// === ADMIN (3)
//		for (int i = 1; i <= 3; i++) {
//			insertUser(
//					"Admin " + i,
//					"admin" + i,
//					"admin" + i + "@mail.com",
//					"ADMIN"
//					  );
//		}
//
//		// === CUSTOMER (50)
//		for (int i = 1; i <= 50; i++) {
//			insertUser(
//					"Customer " + i,
//					"customer" + i,
//					"customer" + i + "@mail.com",
//					"CUSTOMER"
//					  );
//		}
//
//		// === MERCHANT (50)
//		for (int i = 1; i <= 50; i++) {
//			insertUser(
//					"Merchant " + i,
//					"merchant" + i,
//					"merchant" + i + "@mail.com",
//					"MERCHANT"
//					  );
//		}
//	}
//
//	private void insertUser(String name, String username, String email, String role) {
//		jdbcTemplate.update("""
//            INSERT INTO users
//            (enabled, created_at, name, username, email, password, role)
//            VALUES (true, now(), ?, ?, ?, ?, ?)
//        """,
//							name,
//							username,
//							email,
//							passwordEncoder.encode("password123"),
//							role
//						   );
//	}
//}
//
