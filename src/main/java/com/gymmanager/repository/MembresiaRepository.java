package com.gymmanager.repository;

import com.gymmanager.entity.Membresia;
import com.gymmanager.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {

    List<Membresia> findByCliente(Cliente cliente);

    Optional<Membresia> findByClienteAndEstado(Cliente cliente, Membresia.EstadoMembresia estado);

    // Membresías que vencen en X días (para notificaciones)
    @Query("SELECT m FROM Membresia m WHERE m.fechaFin = :fecha AND m.estado = 'ACTIVA'")
    List<Membresia> findByFechaFin(LocalDate fecha);

    // Membresías activas que ya expiraron
    @Query("SELECT m FROM Membresia m WHERE m.fechaFin < :hoy AND m.estado = 'ACTIVA'")
    List<Membresia> findExpiradas(LocalDate hoy);

    // Membresías por vencer en los próximos días
    @Query("SELECT m FROM Membresia m WHERE m.fechaFin BETWEEN :hoy AND :limite AND m.estado = 'ACTIVA'")
    List<Membresia> findPorVencer(LocalDate hoy, LocalDate limite);
}