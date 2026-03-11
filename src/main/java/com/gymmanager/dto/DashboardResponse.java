package com.gymmanager.dto;

import lombok.Data;
import org.apache.tomcat.util.http.parser.Authorization;

@Data
public class DashboardResponse {
    private Long totalClientes;
    private Long membresiasActivas;
    private Long membresiasPorVencer;
    private Long membresiasExpiradas;
    private Long asistenciasHoy;
    private Double recaudacionMesActual;
    private Double recaudacionTotal;
}