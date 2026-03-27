package com.emiLoan.EMILoan.mapper;

import org.springframework.data.domain.Page;

import com.emiLoan.EMILoan.common.response.PagedResponse;

public class PagedResponseMapper {

    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .meta(PagedResponse.Meta.builder()
                        .pageNumber(page.getNumber())
                        .pageSize(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .numberOfElements(page.getNumberOfElements())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .build();
    }
}