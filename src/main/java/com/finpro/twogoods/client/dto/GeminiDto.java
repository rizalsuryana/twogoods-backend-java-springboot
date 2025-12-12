package com.finpro.twogoods.client.dto;

import lombok.*;

import java.util.List;

public class GeminiDto {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ApiRequest {
		private List<Content> contents;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ApiResponse {
		private List<Candidate> candidates;
		private String modelVersion;
		private String responseId;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Content {
		private List<Part> parts;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Part {
		private String text;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Candidate {
		private Content content;
	}
}
