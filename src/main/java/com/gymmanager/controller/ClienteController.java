package com.gymmanager.controller;

import com.gymmanager.dto.ApiResponse;
import com.gymmanager.dto.Cliente.ClienteRequest;
import com.gymmanager.dto.Cliente.ClienteResponse;
import com.gymmanager.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<ClienteResponse>> crear(
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente creado", clienteService.crear(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTADOR','DUENO')")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Clientes obtenidos", clienteService.listarTodos()));
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listarActivos() {
        return ResponseEntity.ok(ApiResponse.ok("Clientes activos obtenidos", clienteService.listarActivos()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente obtenido", clienteService.obtenerPorId(id)));
    }

    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente obtenido", clienteService.obtenerPorDni(dni)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado", clienteService.actualizar(id, request)));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        clienteService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.ok("Cliente desactivado", null));
    }
}