//package com.finpro.twogoods.seeder;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class CustomerSeed {
//
//	private final JdbcTemplate jdbcTemplate;
//
//	public void seed() {
//		jdbcTemplate.update("""
//            INSERT INTO customer_profiles (user_id, location)
//            SELECT id, 'Jakarta'
//            FROM users
//            WHERE role = 'CUSTOMER'
//        """);
//	}
//}
//
