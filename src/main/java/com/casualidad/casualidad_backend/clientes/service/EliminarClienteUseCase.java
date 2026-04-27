package com.casualidad.casualidad_backend.clientes.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casualidad.casualidad_backend.clientes.domain.exception.RecursoNoEncontradoException;
import com.casualidad.casualidad_backend.clientes.domain.model.Cliente;
import com.casualidad.casualidad_backend.clientes.repository.ClienteRepository;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;

@Service
@RequiredArgsConstructor
public class EliminarClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional
    public String ejecutar(Long id) {
        Cliente cliente = buscarCliente(id);
        
        if (tienePedidosAsociados(id)) {
            aplicarBorradoLogico(cliente);
            return "No se puede eliminar físicamente por historial de pedidos. Se ha marcado como INACTIVO.";
        }

        aplicarBorradoFisico(cliente);
        return "Cliente eliminado físicamente del sistema con éxito.";
    }

    // --- Métodos Privados Atómicos ---

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con ID: " + id));
    }

    private boolean tienePedidosAsociados(Long id) {
        return pedidoRepository.countByClienteIdCliente(id) > 0;
    }

    private void aplicarBorradoLogico(Cliente cliente) {
        // CA 2: Usamos toBuilder para generar una nueva instancia inmutable con el cambio
        Cliente clienteInactivo = cliente.toBuilder()
                .activo(false)
                .build();
        clienteRepository.save(clienteInactivo);
    }

    private void aplicarBorradoFisico(Cliente cliente) {
        // Escenario exitoso: Depuración física si no hay dependencias
        clienteRepository.delete(cliente);
    }
}
