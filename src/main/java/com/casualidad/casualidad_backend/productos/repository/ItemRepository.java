package com.casualidad.casualidad_backend.productos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.casualidad.casualidad_backend.productos.domain.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
}