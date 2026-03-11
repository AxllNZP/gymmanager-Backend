package com.gymmanager.controller;

import com.gymmanager.dto.ApiResponse;
import com.gymmanager.dto.Asistencia.AsistenciaRequest;
import com.gymmanager.dto.Asistencia.AsistenciaResponse;
import com.gymmanager.service.AsistenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
@RequiredArgsConstructor
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    @PostMapping("/entrada")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<AsistenciaResponse>> registrarEntrada(
            @Valid @RequestBody AsistenciaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Entrada registrada",
                asistenciaService.registrarEntrada(request)));
    }

    @PatchMapping("/salida/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<AsistenciaResponse>> registrarSalida(
            @PathVariable Long clienteId) {
        return ResponseEntity.ok(ApiResponse.ok("Salida registrada",
                asistenciaService.registrarSalida(clienteId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','DUENO')")
    public ResponseEntity<ApiResponse<List<AsistenciaResponse>>> listarTodas() {
        return ResponseEntity.ok(ApiResponse.ok("Asistencias obtenidas",
                asistenciaService.listarTodas()));
    }

    @GetMapping("/hoy")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','DUENO')")
    public ResponseEntity<ApiResponse<List<AsistenciaResponse>>> listarHoy() {
        return ResponseEntity.ok(ApiResponse.ok("Asistencias de hoy obtenidas",
                asistenciaService.listarHoy()));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CLIENTE')")
    public ResponseEntity<ApiResponse<List<AsistenciaResponse>>> listarPorCliente(
            @PathVariable Long clienteId) {
        return ResponseEntity.ok(ApiResponse.ok("Asistencias del cliente obtenidas",
                asistenciaService.listarPorCliente(clienteId)));
    }
}