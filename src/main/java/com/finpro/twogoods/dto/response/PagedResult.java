package com.finpro.twogoods.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResult<T> {
	private List<T> data;
	private PagingResponse paging;


//	product merchant helper
public static <T> PagedResult<T> from(Page<T> page) {
	return PagedResult.<T>builder()
			.data(page.getContent())
			.paging(PagingResponse.from(page))
			.build();
}

}
