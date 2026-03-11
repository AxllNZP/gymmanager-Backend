package com.gymmanager.dto.Pago;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PagoRequest {

    @NotNull(message = "La membresía es obligatoria")
    private Long membresiaId;

    @NotNull(message = "El cliente pagador es obligatorio")
    private Long clienteId;

    @NotNull(message = "El método de pago es obligatorio")
    private String metodoPago;

    // Descuento opcional (solo Admin)
    private Double descuento;
    private String motivoDescuento;
}