package com.gymmanager.dto.Pago;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PagoResponse {
    private Long id;
    private Long membresiaId;
    private Long clienteId;
    private String clienteNombre;
    private String clienteApellido;
    private String planNombre;
    private Double montoOriginal;
    private Double descuento;
    private Double monto;
    private String motivoDescuento;
    private String metodoPago;
    private String estado;
    private Boolean correoEnviado;
    private String usuarioRegistroNombre;
    private LocalDateTime fechaPago;
}