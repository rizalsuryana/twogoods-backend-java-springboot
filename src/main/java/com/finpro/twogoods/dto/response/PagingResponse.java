package com.finpro.twogoods.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagingResponse {
	private Integer page;
	private Integer rowsPerPage;
	private Long totalRows;
	private Integer totalPages;
	private Boolean hasNext;
	private Boolean hasPrevious;


//	product merchatn
public static PagingResponse from(Page<?> page) {
	return PagingResponse.builder()
			.page(page.getNumber())
			.rowsPerPage(page.getSize())
			.totalRows(page.getTotalElements())
			.totalPages(page.getTotalPages())
			.hasNext(page.hasNext())
			.hasPrevious(page.hasPrevious())
			.build();
}

}
