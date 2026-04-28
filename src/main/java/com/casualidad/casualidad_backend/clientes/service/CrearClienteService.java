package com.casualidad.casualidad_backend.clientes.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casualidad.casualidad_backend.clientes.domain.exception.TelefonoDuplicadoException;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.domain.model.TelefonoCliente;
import com.casualidad.casualidad_backend.clientes.dto.request.ClienteRequestDto;
import com.casualidad.casualidad_backend.clientes.dto.response.ClienteResponseDto;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.clientes.repository.TelefonoClienteRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrearClienteService {

   private final ClienteRepository clienteRepository;
    private final TelefonoClienteRepository telefonoRepository;

    @Transactional
    public ClienteResponseDto registrarCliente(ClienteRequestDto dto) {
        
        Set<String> telefonosUnicosRequest = new HashSet<>(dto.telefonos());
        
        List<TelefonoCliente> telefonosExistentes = telefonoRepository.findByNumeroTelefonoIn(dto.telefonos());
        if (!telefonosExistentes.isEmpty()) {
            throw new TelefonoDuplicadoException("Uno o más números de teléfono ya están registrados en otros clientes.");
        }

        Cliente cliente = Cliente.builder()
                .nombre(dto.nombre())
                .direccion(dto.direccion())
                .activo(true)
                .build();

        List<TelefonoCliente> listaTelefonosEntidad = telefonosUnicosRequest.stream()
                .map(num -> TelefonoCliente.builder()
                        .numeroTelefono(num)
                        .cliente(cliente)
                        .build())
                .collect(Collectors.toList());

        cliente.getTelefonos().addAll(listaTelefonosEntidad);

        Cliente guardado = clienteRepository.save(cliente);

        List<String> telefonosResponse = guardado.getTelefonos().stream()
                .map(TelefonoCliente::getNumeroTelefono)
                .collect(Collectors.toList());

        return new ClienteResponseDto(
                guardado.getIdCliente(),
                guardado.getNombre(),
                guardado.getDireccion(),
                telefonosResponse
        );
    }
}
