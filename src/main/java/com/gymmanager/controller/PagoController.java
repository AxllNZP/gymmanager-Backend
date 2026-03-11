package com.gymmanager.controller;

import com.gymmanager.dto.ApiResponse;
import com.gymmanager.dto.Pago.PagoRequest;
import com.gymmanager.dto.Pago.PagoResponse;
import com.gymmanager.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<PagoResponse>> registrar(
            @Valid @RequestBody PagoRequest request,
            Authentication authentication) {
        String emailUsuario = authentication.getName();
        return ResponseEntity.ok(ApiResponse.ok("Pago registrado",
                pagoService.registrar(request, emailUsuario)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR','DUENO','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Pagos obtenidos",
                pagoService.listarTodos()));
    }

    @GetMapping("/mes-actual")
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR','DUENO','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPorMes() {
        return ResponseEntity.ok(ApiResponse.ok("Pagos del mes obtenidos",
                pagoService.listarPorMes()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR','DUENO','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<PagoResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Pago obtenido",
                pagoService.obtenerPorId(id)));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','CONTADOR','CLIENTE')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPorCliente(
            @PathVariable Long clienteId) {
        return ResponseEntity.ok(ApiResponse.ok("Pagos del cliente obtenidos",
                pagoService.listarPorCliente(clienteId)));
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> anular(
            @PathVariable Long id,
            Authentication authentication) {
        pagoService.anular(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("Pago anulado", null));
    }
}