package com.casualidad.casualidad_backend.pagos.repository;

import com.casualidad.casualidad_backend.pagos.domain.model.AuditoriaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaPagoRepository extends JpaRepository<AuditoriaPago, Long> {
}
