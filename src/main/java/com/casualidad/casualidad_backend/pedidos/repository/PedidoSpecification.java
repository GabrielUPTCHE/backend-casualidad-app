package com.casualidad.casualidad_backend.pedidos.repository;

import com.casualidad.casualidad_backend.pedidos.domain.model.Pedido;
import com.casualidad.casualidad_backend.pedidos.dto.request.FiltroPedidoDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PedidoSpecification {

    public static Specification<Pedido> conFiltros(FiltroPedidoDto filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por Cliente
            if (filtro.idCliente() != null) {
                predicates.add(cb.equal(root.get("cliente").get("idCliente"), filtro.idCliente()));
            }

            // Filtro por Estado
            if (filtro.estado() != null) {
                predicates.add(cb.equal(root.get("estadoPedido"), filtro.estado()));
            }

            // Filtro por Rango de Fechas (usamos fechaEntrega como referencia de producción)
            if (filtro.fechaInicio() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaEntrega"), filtro.fechaInicio()));
            }
            if (filtro.fechaFin() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaEntrega"), filtro.fechaFin()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
