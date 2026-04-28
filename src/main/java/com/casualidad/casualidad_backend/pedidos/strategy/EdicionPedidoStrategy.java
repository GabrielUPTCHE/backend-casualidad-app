package com.casualidad.casualidad_backend.pedidos.strategy;

import com.casualidad.casualidad_backend.common.domain.enums.EstadoPedido;
import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.dto.request.ActualizarPedidoDto;

public interface EdicionPedidoStrategy {
    EstadoPedido getEstadoSoportado();
    void editar(Pedido pedido, ActualizarPedidoDto request);
}