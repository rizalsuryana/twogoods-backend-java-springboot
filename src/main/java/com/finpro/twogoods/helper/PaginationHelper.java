package com.finpro.twogoods.helper;

import com.finpro.twogoods.dto.response.PagingResponse;
import org.springframework.data.domain.Page;

public class PaginationHelper {

	public static PagingResponse fromPage(Page<?> page) {
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
