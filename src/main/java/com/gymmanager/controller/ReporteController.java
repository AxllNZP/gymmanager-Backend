package com.gymmanager.controller;

import com.gymmanager.dto.ApiResponse;
import com.gymmanager.dto.DashboardResponse;
import com.gymmanager.dto.Membresia.MembresiaResponse;
import com.gymmanager.dto.Pago.PagoResponse;
import com.gymmanager.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','DUENO','CONTADOR','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok("Dashboard obtenido",
                reporteService.obtenerDashboard()));
    }

    @GetMapping("/pagos")
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR','DUENO','RECEPCIONISTA')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> reportePagos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(ApiResponse.ok("Reporte de pagos obtenido",
                reporteService.reportePagosPorPeriodo(inicio, fin)));
    }

    @GetMapping("/membresias/expiradas")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','DUENO')")
    public ResponseEntity<ApiResponse<List<MembresiaResponse>>> membresiasExpiradas() {
        return ResponseEntity.ok(ApiResponse.ok("Membresías expiradas obtenidas",
                reporteService.reporteMembresiasExpiradas()));
    }

    @GetMapping("/membresias/por-vencer")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA','DUENO')")
    public ResponseEntity<ApiResponse<List<MembresiaResponse>>> membresiasPorVencer() {
        return ResponseEntity.ok(ApiResponse.ok("Membresías por vencer obtenidas",
                reporteService.reporteMembresiasPorVencer()));
    }

    @GetMapping("/exportar/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR','DUENO')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        byte[] pdf = reporteService.exportarPagosPdf(inicio, fin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte_pagos_" + inicio + "_" + fin + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/exportar/excel")
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR','DUENO')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        byte[] excel = reporteService.exportarPagosExcel(inicio, fin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte_pagos_" + inicio + "_" + fin + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}