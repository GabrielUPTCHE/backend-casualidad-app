package com.casualidad.casualidad_backend.clientes.service;

import com.casualidad.casualidad_backend.clientes.domain.exception.RecursoNoEncontradoException;
import com.casualidad.casualidad_backend.clientes.domain.exception.TelefonoDuplicadoException;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.domain.model.TelefonoCliente;
import com.casualidad.casualidad_backend.clientes.dto.request.ClienteRequestDto;
import com.casualidad.casualidad_backend.clientes.dto.response.ClienteResponseDto;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.clientes.repository.TelefonoClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActualizarClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final TelefonoClienteRepository telefonoRepository;

    @Transactional
    public ClienteResponseDto ejecutar(Long idCliente, ClienteRequestDto dto) {
        Cliente clienteActual = buscarCliente(idCliente);
        validarDuplicidadDeTelefonos(dto.telefonos(), idCliente);

        Cliente clienteActualizado = construirClienteActualizado(clienteActual, dto);
        actualizarColeccionTelefonos(clienteActualizado, dto.telefonos());

        Cliente guardado = clienteRepository.save(clienteActualizado);
        return mapearARespuesta(guardado);
    }


    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
    }

    private void validarDuplicidadDeTelefonos(List<String> telefonos, Long idCliente) {
        List<TelefonoCliente> ajenos = telefonoRepository.findTelefonosDeOtrosClientes(telefonos, idCliente);
        if (!ajenos.isEmpty()) {
            throw new TelefonoDuplicadoException("El teléfono " + ajenos.get(0).getNumeroTelefono() + " pertenece a otro cliente.");
        }
    }

    private Cliente construirClienteActualizado(Cliente actual, ClienteRequestDto dto) {
        // toBuilder() crea una copia exacta, sobreescribimos nombre y dirección, 
        // pero mantenemos el mismo ID para que JPA sepa que es un UPDATE.
        return actual.toBuilder()
                .nombre(dto.nombre())
                .direccion(dto.direccion())
                .build();
    }

    private void actualizarColeccionTelefonos(Cliente cliente, List<String> nuevosNumeros) {
        // En JPA, no podemos reasignar una colección que tiene orphanRemoval=true usando Builder.
        // La forma "Experta" es mutar la colección persistente vaciándola y llenándola de nuevo.
        cliente.getTelefonos().clear();
        
        List<TelefonoCliente> nuevasEntidades = nuevosNumeros.stream()
                .distinct()
                .map(num -> TelefonoCliente.builder()
                        .numeroTelefono(num)
                        .cliente(cliente)
                        .build())
                .toList(); // .toList() en lugar de Collectors (Java 16+)

        cliente.getTelefonos().addAll(nuevasEntidades);
    }

    private ClienteResponseDto mapearARespuesta(Cliente cliente) {
        List<String> numeros = cliente.getTelefonos().stream()
                .map(TelefonoCliente::getNumeroTelefono)
                .toList();

        return new ClienteResponseDto(cliente.getIdCliente(), cliente.getNombre(), cliente.getDireccion(), numeros);
    }
}
