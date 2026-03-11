package com.gymmanager.controller;

import com.gymmanager.dto.ApiResponse;
import com.gymmanager.entity.AuditLog;
import com.gymmanager.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Logs obtenidos",
                auditLogService.listarTodos()));
    }

    @GetMapping("/usuario/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> listarPorUsuario(
            @PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.ok("Logs del usuario obtenidos",
                auditLogService.listarPorUsuario(email)));
    }

    @GetMapping("/entidad/{entidad}/{entidadId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> listarPorEntidad(
            @PathVariable String entidad,
            @PathVariable String entidadId) {
        return ResponseEntity.ok(ApiResponse.ok("Logs de entidad obtenidos",
                auditLogService.listarPorEntidad(entidad, entidadId)));
    }
}