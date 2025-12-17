package com.finpro.twogoods.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;
	private final UserSeed userSeed;
	private final CustomerSeed customerSeed;
	private final MerchantSeed merchantSeed;
	private final ProductSeed productSeed;

	@Override
	public void run(ApplicationArguments args) {
		seed("V1_USERS", userSeed::seed);
		seed("V2_CUSTOMERS", customerSeed::seed);
		seed("V3_MERCHANTS", merchantSeed::seed);
		seed("V4_PRODUCTS", productSeed::seed);
	}

	private void seed(String name, Runnable action) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM seed_history WHERE seed_name = ?",
				Integer.class,
				name
												   );

		if (count != null && count > 0) {
			System.out.println("⏭ " + name + " already seeded");
			return;
		}

		action.run();

		jdbcTemplate.update(
				"INSERT INTO seed_history VALUES (?, now())",
				name
						   );

		System.out.println("✅ " + name + " seeded");
	}
}

