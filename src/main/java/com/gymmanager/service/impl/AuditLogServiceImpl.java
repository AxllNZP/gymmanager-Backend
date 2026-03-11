package com.gymmanager.service.impl;

import com.gymmanager.entity.AuditLog;
import com.gymmanager.repository.AuditLogRepository;
import com.gymmanager.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Override
    public void registrar(String usuarioEmail, String accion, String entidad,
                          String entidadId, String detalle,
                          String ipAddress, String resultado) {
        try {
            AuditLog log = new AuditLog();
            log.setUsuarioEmail(usuarioEmail);
            log.setAccion(accion);
            log.setEntidad(entidad);
            log.setEntidadId(entidadId);
            log.setDetalle(detalle);
            log.setIpAddress(ipAddress);
            log.setResultado(resultado);
            auditLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error registrando audit log: {}", e.getMessage());
        }
    }

    @Override
    public List<AuditLog> listarPorUsuario(String email) {
        return auditLogRepository.findByUsuarioEmail(email);
    }

    @Override
    public List<AuditLog> listarPorEntidad(String entidad, String entidadId) {
        return auditLogRepository.findByEntidadAndEntidadId(entidad, entidadId);
    }

    @Override
    public List<AuditLog> listarTodos() {
        return auditLogRepository.findAll();
    }
}