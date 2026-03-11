package com.gymmanager.repository;

import com.gymmanager.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUsuarioEmail(String email);

    List<AuditLog> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    List<AuditLog> findByEntidadAndEntidadId(String entidad, String entidadId);
}