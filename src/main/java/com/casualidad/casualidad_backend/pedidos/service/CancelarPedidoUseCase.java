package com.casualidad.casualidad_backend.pedidos.service;

import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import com.casualidad.casualidad_backend.inventario.service.GestionInventarioPedidoService;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final GestionInventarioPedidoService inventarioService;

    @Transactional
    public void ejecutar(Long idPedido, boolean reintegrarMateriales) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        EstadoPedido estadoAnterior = pedido.getEstadoPedido();

        if (estadoAnterior == EstadoPedido.TERMINADO || estadoAnterior == EstadoPedido.TERMINADO) {
            throw new IllegalStateException("No se puede cancelar un pedido completado o entregado.");
        }

        if (estadoAnterior == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("El pedido ya se encuentra cancelado.");
        }

        // CA 3: Si estaba en producción y se aprueba el reintegro, devolver materiales
        if (estadoAnterior == EstadoPedido.EN_PRODUCCION && reintegrarMateriales) {
            pedido.getDetalles().forEach(detalle -> 
                inventarioService.reintegrarInventario(detalle.getProducto(), detalle.getCantidad())
            );
        } else if (estadoAnterior == EstadoPedido.EN_PRODUCCION && !reintegrarMateriales) {
            // Nota: Aquí podrías lanzar una excepción si el negocio dicta que la pregunta es ESTRICTAMENTE OBLIGATORIA 
            // y no se puede cancelar sin responder explícitamente a ella desde el front.
        }

        pedido.setEstadoPedido(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }
}