package com.casualidad.casualidad_backend.productos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.casualidad.casualidad_backend.productos.domain.model.ProductoItem;
import com.casualidad.casualidad_backend.productos.domain.model.ProductoItemId;

import java.util.List;

@Repository
public interface ProductoItemRepository extends JpaRepository<ProductoItem, ProductoItemId> {

    /**
     * Busca toda la "receta" de un producto específico.
     * Útil para calcular el costo total de producción (CA2).
     */
    List<ProductoItem> findByProductoIdProducto(Long idProducto);

    /**
     * Busca en qué "recetas" se utiliza un insumo específico.
     * Útil para el Listener (CA3): Si el precio de un insumo sube, 
     * necesitamos saber qué productos actualizar.
     */
    List<ProductoItem> findByItemIdItem(Long idItem);

    /**
     * Elimina toda la composición anterior de un producto.
     * Se usa @Modifying y una @Query explícita por rendimiento (Performance).
     * Si usáramos el método derivado 'deleteByProductoIdProducto', Hibernate 
     * haría un SELECT primero y luego un DELETE por cada registro (N+1).
     * Con esta @Query, hace un único DELETE directo a la base de datos.
     */
    @Modifying
    @Query("DELETE FROM ProductoItem pi WHERE pi.producto.idProducto = :idProducto")
    void deleteByProductoIdProducto(@Param("idProducto") Long idProducto);
}
