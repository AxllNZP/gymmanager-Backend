package com.gymmanager.dto.Asistencia;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsistenciaRequest {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    // Opcional: para integración futura con torniquete
    private String registradoPor;
}