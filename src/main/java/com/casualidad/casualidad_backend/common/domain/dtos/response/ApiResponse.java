package com.casualidad.casualidad_backend.common.domain.dtos.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
    private String message;
    private int code;
    private T data;
}
