package com.finpro.twogoods.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductSeed {

	private final JdbcTemplate jdbcTemplate;

	public void seed() {

		// === PRODUCTS
		jdbcTemplate.update("""
            INSERT INTO products
            (created_at, name, description, price, is_available, condition, merchant_id)
            SELECT
                now(),
                'Product ' || gs,
                'Dummy product description',
                (random() * 500000)::numeric(38,2),
                true,
                CASE WHEN random() > 0.5 THEN 'NEW' ELSE 'USED' END,
                mp.user_id
            FROM generate_series(1,200) gs
            JOIN merchant_profile mp ON random() < 0.02
        """);

		// === IMAGES
		jdbcTemplate.update("""
            INSERT INTO product_images
            (created_at, image_url, product_id)
            SELECT
                now(),
                'https://picsum.photos/400/400?random=' || p.id,
                p.id
            FROM products p
        """);

		// === CATEGORIES
		jdbcTemplate.update("""
            INSERT INTO product_categories (product_id, categories)
            SELECT
                id,
                (ARRAY['Men','Women','Child','Baby','Shirt','Pants','Accessory','Unisex'])
                [floor(random()*8)+1]
            FROM products
        """);
	}
}

