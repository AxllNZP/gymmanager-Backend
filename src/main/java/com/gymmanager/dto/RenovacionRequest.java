package com.gymmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class RenovacionRequest {

    @NotNull(message = "El plan es obligatorio")
    private Long planId;

    // Puede cambiar los clientes adicionales al renovar
    private List<Long> clientesAdicionalesIds;
}