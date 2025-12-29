//package com.finpro.twogoods.seeder;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class MerchantSeed {
//
//	private final JdbcTemplate jdbcTemplate;
//
//	public void seed() {
//		jdbcTemplate.update("""
//            INSERT INTO merchant_profile
//            (user_id, is_verified, location, ktp_photo, nomor_ktp)
//            SELECT
//                id,
//                (random() * 3)::int,
//                'Bandung',
//                'https://picsum.photos/300/200',
//                '3201' || floor(random() * 100000000)::text
//            FROM users
//            WHERE role = 'MERCHANT'
//        """);
//	}
//}
//
