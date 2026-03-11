package com.gymmanager.service;

import com.gymmanager.entity.AuditLog;
import java.util.List;

public interface AuditLogService {
    void registrar(String usuarioEmail, String accion, String entidad,
                   String entidadId, String detalle, String ipAddress, String resultado);
    List<AuditLog> listarPorUsuario(String email);
    List<AuditLog> listarPorEntidad(String entidad, String entidadId);
    List<AuditLog> listarTodos();
}