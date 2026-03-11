package com.gymmanager.dto.Asistencia;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AsistenciaResponse {
    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private String clienteApellido;
    private String clienteDni;
    private Long membresiaId;
    private String estadoMembresia;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private String registradoPor;
}