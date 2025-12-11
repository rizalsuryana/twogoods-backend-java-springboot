package com.finpro.twogoods.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finpro.twogoods.client.GeminiFeignClient;
import com.finpro.twogoods.client.dto.GeminiDto;
import com.finpro.twogoods.dto.request.SuggestPriceRequest;
import com.finpro.twogoods.dto.response.SuggestPriceResponse;
import com.finpro.twogoods.entity.Product;
import com.finpro.twogoods.enums.Categories;
import com.finpro.twogoods.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPriceService {

	private final ProductRepository productRepository;
	private final GeminiFeignClient geminiClient;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public SuggestPriceResponse suggestPrice(SuggestPriceRequest req) {

		//  1. Fetch similar products
		Categories mainCategory = req.getCategories().get(0);

		List<Product> similar = productRepository.findSimilarProducts(
				mainCategory,
				req.getCondition()
		);


		if (similar.isEmpty()) {
			return SuggestPriceResponse.builder()
					.recommendedPrice(BigDecimal.ZERO)
					.minRange(BigDecimal.ZERO)
					.maxRange(BigDecimal.ZERO)
					.reasoning("No similar products found in the database.")
					.build();
		}

		//  2. Extract price statistics
		List<BigDecimal> prices = similar.stream()
				.map(Product::getPrice)
				.toList();

		BigDecimal min = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		BigDecimal max = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		BigDecimal avg = prices.stream()
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);

		//  3. Build AI prompt (English)
		String prompt = """
                You are an AI assistant that recommends product prices based on similar items.

                Product details:
                - Name: %s
                - Description: %s
                - Categories: %s
                - Condition: %s

                Similar product price statistics:
                - Minimum price: %s
                - Maximum price: %s
                - Average price: %s

                Based on the data above, generate a JSON response with the following structure:

                {
                  "recommendedPrice": number,
                  "minRange": number,
                  "maxRange": number,
                  "reasoning": "string"
                }

                Only return valid JSON.
                """.formatted(
				req.getName(),
				req.getDescription(),
				req.getCategories(),
				req.getCondition(),
				min, max, avg
		);

		//  4. Build Gemini request
		GeminiDto.ApiRequest aiRequest = GeminiDto.ApiRequest.builder()
				.contents(List.of(
						GeminiDto.Content.builder()
								.parts(List.of(
										GeminiDto.Part.builder()
												.text(prompt)
												.build()
								))
								.build()
				))
				.build();

		//  5. Call Gemini
		GeminiDto.ApiResponse aiResponse = geminiClient.askGemini(aiRequest);

		String aiText = aiResponse.getCandidates()
				.get(0)
				.getContent()
				.getParts()
				.get(0)
				.getText();

		//  6. Parse JSON from AI
		try {
			return objectMapper.readValue(aiText, SuggestPriceResponse.class);
		} catch (Exception e) {
			return SuggestPriceResponse.builder()
					.recommendedPrice(avg)
					.minRange(min)
					.maxRange(max)
					.reasoning("AI returned invalid JSON. Using statistical fallback.")
					.build();
		}
	}
}
