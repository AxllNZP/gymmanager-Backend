package com.gymmanager.service;
import com.gymmanager.exception.ResourceNotFoundException;
import com.gymmanager.exception.DuplicateResourceException;
import com.gymmanager.exception.InvalidOperationException;
import com.gymmanager.dto.DashboardResponse;
import com.gymmanager.dto.Membresia.MembresiaResponse;
import com.gymmanager.dto.Pago.PagoResponse;
import com.gymmanager.dto.Membresia.MembresiaResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {
    DashboardResponse obtenerDashboard();
    List<PagoResponse> reportePagosPorPeriodo(LocalDate inicio, LocalDate fin);
    List<MembresiaResponse> reporteMembresiasExpiradas();
    List<MembresiaResponse> reporteMembresiasPorVencer();
    byte[] exportarPagosPdf(LocalDate inicio, LocalDate fin);
    byte[] exportarPagosExcel(LocalDate inicio, LocalDate fin);
}