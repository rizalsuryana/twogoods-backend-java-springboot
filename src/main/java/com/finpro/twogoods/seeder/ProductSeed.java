package com.finpro.twogoods.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

record ProductTemplate(
		String name,
		String description,
		String category,
		String imageUrl
) {}

@Component
@RequiredArgsConstructor
public class ProductSeed {

	private final JdbcTemplate jdbcTemplate;

	private final List<ProductTemplate> products = List.of(
			new ProductTemplate(
					"Men's Cotton T-Shirt",
					"Comfortable breathable cotton t-shirt",
					"Men",
					"https://images.unsplash.com/photo-1521572163474-6864f9cf17ab"
			),
			new ProductTemplate(
					"Women's Casual Blouse",
					"Lightweight casual blouse for daily wear",
					"Women",
					"https://images.unsplash.com/photo-1520975916090-3105956dac38"
			),
			new ProductTemplate(
					"Baby Cotton Onesie",
					"Soft cotton onesie for babies",
					"Baby",
					"https://images.unsplash.com/photo-1602810318383-e386cc6f1d69"
			),
			new ProductTemplate(
					"Kids Denim Pants",
					"Durable denim pants for kids",
					"Child",
					"https://images.unsplash.com/photo-1618354691303-dc1b1c63e3db"
			),
			new ProductTemplate(
					"Unisex Baseball Cap",
					"Classic adjustable baseball cap",
					"Accessory",
					"https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"
			),
			new ProductTemplate(
					"Men's Slim Fit Jeans",
					"Stylish slim fit denim jeans",
					"Pants",
					"https://images.unsplash.com/photo-1512436991641-6745cdb1723f"
			),
			new ProductTemplate(
					"Women's Summer Dress",
					"Floral summer dress",
					"Women",
					"https://images.unsplash.com/photo-1520975869019-4b8ec9f8a3a0"
			),
			new ProductTemplate(
					"Leather Wallet",
					"Genuine leather wallet",
					"Accessory",
					"https://images.unsplash.com/photo-1598032895397-b9472444bf93"
			)
														  );

	public void seed() {

		// Ambil semua merchant
		List<Long> merchantIds = jdbcTemplate.queryForList(
				"SELECT user_id FROM merchant_profile",
				Long.class
														  );

		if (merchantIds.isEmpty()) return;

		Random random = new Random();

		for (int i = 0; i < 200; i++) {
			ProductTemplate p = products.get(random.nextInt(products.size()));
			Long merchantId = merchantIds.get(random.nextInt(merchantIds.size()));

			Long productId = jdbcTemplate.queryForObject("""
                INSERT INTO products
                (created_at, name, description, price, is_available, condition, merchant_id)
                VALUES (now(), ?, ?, ?, true, ?, ?)
                RETURNING id
            """,
														 Long.class,
														 p.name(),
														 p.description(),
														 BigDecimal.valueOf(50_000 + random.nextInt(450_000)),
														 random.nextBoolean() ? "NEW" : "USED",
														 merchantId
														);

			// Image
			jdbcTemplate.update("""
                INSERT INTO product_images (created_at, image_url, product_id)
                VALUES (now(), ?, ?)
            """, p.imageUrl() + "?w=600", productId);

			// Category
			jdbcTemplate.update("""
                INSERT INTO product_categories (product_id, categories)
                VALUES (?, ?)
            """, productId, p.category());
		}
	}
}
