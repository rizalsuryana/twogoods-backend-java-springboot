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

		Categories mainCategory = req.getCategories().get(0);

		List<Product> similar = productRepository.findSimilarProducts(
				mainCategory,
				req.getCondition()
		);

		//  Jika tidak ada data → AI murni (Rupiah)
		if (similar.isEmpty()) {
			return askAiWithoutDatabase(req);
		}

		//  Jika ada data → hitung statistik
		List<BigDecimal> prices = similar.stream()
				.map(Product::getPrice)
				.toList();

		BigDecimal min = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		BigDecimal max = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		BigDecimal avg = prices.stream()
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);

		String prompt = """
                You are an AI assistant that recommends product prices for the Indonesian marketplace.

                Product details:
                - Name: %s
                - Description: %s
                - Category: %s
                - Condition: %s

                Similar product price statistics (in Indonesian Rupiah):
                - Minimum price: %s
                - Maximum price: %s
                - Average price: %s

                IMPORTANT RULES:
                - All prices MUST be in Indonesian Rupiah (IDR)
                - Use plain numbers only (e.g., 75000), no commas, no dots, no currency symbols
                - Prices must be realistic for the Indonesian second-hand market
                - Do NOT output decimals

                Respond ONLY with valid JSON:

                {
                  "recommendedPrice": number,
                  "minRange": number,
                  "maxRange": number,
                  "reasoning": "string"
                }
                """.formatted(
				req.getName(),
				req.getDescription(),
				mainCategory,
				req.getCondition(),
				min, max, avg
		);

		return callAiAndParse(prompt, avg, min, max);
	}

	//  AI fallback ketika database kosong (Rupiah)
	private SuggestPriceResponse askAiWithoutDatabase(SuggestPriceRequest req) {

		String prompt = """
                You are an AI assistant that recommends product prices for the Indonesian marketplace.

                Product details:
                - Name: %s
                - Description: %s
                - Category: %s
                - Condition: %s

                There are no similar products in the database.

                Based on general market knowledge in INDONESIA, estimate a reasonable price range in Indonesian Rupiah (IDR).

                IMPORTANT RULES:
                - All prices MUST be in Indonesian Rupiah (IDR)
                - Use plain numbers only (e.g., 75000), no commas, no dots, no currency symbols
                - Prices must be realistic for the Indonesian second-hand market
                - Do NOT output decimals

                Respond ONLY with valid JSON:

                {
                  "recommendedPrice": number,
                  "minRange": number,
                  "maxRange": number,
                  "reasoning": "string"
                }
                """.formatted(
				req.getName(),
				req.getDescription(),
				req.getCategories().get(0),
				req.getCondition()
		);

		return callAiAndParse(prompt, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
	}

	//  Method pemanggil AI + sanitizer JSON
	private SuggestPriceResponse callAiAndParse(String prompt, BigDecimal avg, BigDecimal min, BigDecimal max) {

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

		GeminiDto.ApiResponse aiResponse = geminiClient.askGemini(aiRequest);

		String aiText = aiResponse.getCandidates()
				.get(0)
				.getContent()
				.getParts()
				.get(0)
				.getText()
				.trim();

		//  Sanitizer
		aiText = aiText.replace("```json", "")
				.replace("```", "")
				.trim();

		int start = aiText.indexOf("{");
		int end = aiText.lastIndexOf("}");
		if (start != -1 && end != -1) {
			aiText = aiText.substring(start, end + 1);
		}

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
