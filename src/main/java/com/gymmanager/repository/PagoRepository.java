package com.gymmanager.repository;

import com.gymmanager.entity.Pago;
import com.gymmanager.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByCliente(Cliente cliente);

    List<Pago> findByFechaPagoBetween(LocalDateTime inicio, LocalDateTime fin);

    // Total recaudado en un período
    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.fechaPago BETWEEN :inicio AND :fin AND p.estado = 'COMPLETADO'")
    Double sumMontoByPeriodo(LocalDateTime inicio, LocalDateTime fin);

    // Pagos del mes actual
    @Query("SELECT p FROM Pago p WHERE MONTH(p.fechaPago) = MONTH(CURRENT_DATE) AND YEAR(p.fechaPago) = YEAR(CURRENT_DATE)")
    List<Pago> findPagosMesActual();
}