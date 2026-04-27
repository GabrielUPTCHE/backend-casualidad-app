package com.casualidad.casualidad_backend.clientes.service;

import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.domain.model.TelefonoCliente;
import com.casualidad.casualidad_backend.clientes.dto.response.ClienteListadoDto;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.common.domain.dtos.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarClientesUseCase {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public PageResponse<ClienteListadoDto> ejecutar(int page, int size, String filtro) {
        Pageable paginacion = PageRequest.of(page, size);
        Page<Cliente> paginaClientes = clienteRepository.buscarActivosPorFiltro(filtro, paginacion);
        
        return construirRespuestaPaginada(paginaClientes);
    }

    // --- Métodos Privados Atómicos ---

    private PageResponse<ClienteListadoDto> construirRespuestaPaginada(Page<Cliente> pagina) {
        List<ClienteListadoDto> data = pagina.getContent().stream()
                .map(this::mapearAClienteListadoDto)
                .toList();

        return PageResponse.<ClienteListadoDto>builder()
                .pageNumber(pagina.getNumber())
                .pageSize(pagina.getSize())
                .totalElements(pagina.getTotalElements())
                .totalPages(pagina.getTotalPages())
                .data(data)
                .build();
    }

    private ClienteListadoDto mapearAClienteListadoDto(Cliente cliente) {
        List<String> numeros = cliente.getTelefonos().stream()
                .map(TelefonoCliente::getNumeroTelefono)
                .toList();

        return ClienteListadoDto.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .direccion(cliente.getDireccion())
                .telefonos(numeros)
                .build();
    }
}