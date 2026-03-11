package com.gymmanager.dto.Membresia;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class MembresiaRequest {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El plan es obligatorio")
    private Long planId;

    // IDs de clientes adicionales (para planes de 2 o 3 personas)
    private List<Long> clientesAdicionalesIds;
}