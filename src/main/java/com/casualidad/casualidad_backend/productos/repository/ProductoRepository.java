package com.casualidad.casualidad_backend.productos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.casualidad.casualidad_backend.common.domain.enums.TipoProducto;
import com.casualidad.casualidad_backend.productos.domain.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByNombreIgnoreCase(String nombre); // CA 2
    boolean existsByTipo(TipoProducto tipo); // CA 9
    boolean existsByNombreIgnoreCaseAndIdProductoNot(String nombre, Long idProducto);
    @Query("SELECT p FROM Producto p " +
           "WHERE (CAST(:nombre AS string) IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%'))) " +
           "AND (:tipo IS NULL OR p.tipo = :tipo)")
    Page<Producto> buscarInventarioConFiltros(
            @Param("nombre") String nombre, 
            @Param("tipo") TipoProducto tipo, 
            Pageable pageable
    );
}