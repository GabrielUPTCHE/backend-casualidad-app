package com.casualidad.casualidad_backend.pagos.controller;

import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;
import com.casualidad.casualidad_backend.pagos.dto.response.PagoDTO;
import com.casualidad.casualidad_backend.pagos.service.PagoService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<PageResponse<PagoDTO>> listarPagos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(pagoService.listarPagosPaginados(page, size));
    }
}
