package com.gymmanager.repository;

import com.gymmanager.entity.Asistencia;
import com.gymmanager.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findByCliente(Cliente cliente);

    List<Asistencia> findByFechaEntradaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Asistencia> findByClienteAndFechaEntradaBetween(Cliente cliente, LocalDateTime inicio, LocalDateTime fin);
}