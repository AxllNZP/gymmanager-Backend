package com.gymmanager.controller;

import com.gymmanager.dto.ApiResponse;
import com.gymmanager.dto.Plan.PlanRequest;
import com.gymmanager.dto.Plan.PlanResponse;
import com.gymmanager.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planes")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlanResponse>> crear(
            @Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Plan creado", planService.crear(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTADOR','DUENO')")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> listarActivos() {
        return ResponseEntity.ok(ApiResponse.ok("Planes obtenidos", planService.listarActivos()));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Planes obtenidos", planService.listarTodos()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<PlanResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Plan obtenido", planService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlanResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Plan actualizado", planService.actualizar(id, request)));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        planService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.ok("Plan desactivado", null));
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activar(@PathVariable Long id) {
        planService.activar(id);
        return ResponseEntity.ok(ApiResponse.ok("Plan activado", null));
    }
}