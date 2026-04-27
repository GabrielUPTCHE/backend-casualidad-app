package com.casualidad.casualidad_backend.common.domain.dtos.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PageResponse<T> {
    private final Integer pageNumber;
    private final Integer pageSize;
    private final Long totalElements;
    private final Integer totalPages;
    private final List<T> data;
}
