package com.gymmanager.controller;

import com.gymmanager.dto.ApiResponse;
import com.gymmanager.dto.Membresia.MembresiaRequest;
import com.gymmanager.dto.Membresia.MembresiaResponse;
import com.gymmanager.dto.RenovacionRequest;
import com.gymmanager.service.MembresiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membresias")
@RequiredArgsConstructor
public class MembresiaController {

    private final MembresiaService membresiaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<MembresiaResponse>> crear(
            @Valid @RequestBody MembresiaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Membresía creada",
                membresiaService.crear(request)));
    }

    @PostMapping("/{id}/renovar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<MembresiaResponse>> renovar(
            @PathVariable Long id,
            @Valid @RequestBody RenovacionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Membresía renovada",
                membresiaService.renovar(id, request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTADOR','DUENO')")
    public ResponseEntity<ApiResponse<List<MembresiaResponse>>> listarTodas() {
        return ResponseEntity.ok(ApiResponse.ok("Membresías obtenidas",
                membresiaService.listarTodas()));
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<List<MembresiaResponse>>> listarActivas() {
        return ResponseEntity.ok(ApiResponse.ok("Membresías activas obtenidas",
                membresiaService.listarActivas()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<MembresiaResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Membresía obtenida",
                membresiaService.obtenerPorId(id)));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CLIENTE')")
    public ResponseEntity<ApiResponse<List<MembresiaResponse>>> obtenerPorCliente(
            @PathVariable Long clienteId) {
        return ResponseEntity.ok(ApiResponse.ok("Membresías del cliente obtenidas",
                membresiaService.obtenerPorCliente(clienteId)));
    }
}